package net.geforcemods.securitycraft.blocks.mines;

import java.util.stream.Stream;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.blockentities.ClaymoreBlockEntity;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ClaymoreBlock extends ExplosiveBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty DEACTIVATED = BooleanProperty.create("deactivated");
	private static final VoxelShape NORTH = Stream.of(Block.box(4, 0, 8, 12, 6, 9), Block.box(5, 0, 7, 11, 6, 8), Block.box(6, 6, 8, 10, 7, 9)).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
	private static final VoxelShape EAST = Stream.of(Block.box(7, 0, 4, 8, 6, 12), Block.box(8, 0, 5, 9, 6, 11), Block.box(7, 6, 6, 8, 7, 10)).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
	private static final VoxelShape SOUTH = Stream.of(Block.box(4, 0, 7, 12, 6, 8), Block.box(5, 0, 8, 11, 6, 9), Block.box(6, 6, 7, 10, 7, 8)).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
	private static final VoxelShape WEST = Stream.of(Block.box(8, 0, 4, 9, 6, 12), Block.box(7, 0, 5, 8, 6, 11), Block.box(8, 6, 6, 9, 7, 10)).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

	public ClaymoreBlock(Block.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DEACTIVATED, false));
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag) {
		if (world.getBlockState(pos.below()).getMaterial() != Material.AIR)
			return;
		else
			world.destroyBlock(pos, true);
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		return BlockUtils.isSideSolid(world, pos.below(), Direction.UP);
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (!player.isCreative() && !world.isClientSide && !world.getBlockState(pos).getValue(ClaymoreBlock.DEACTIVATED)) {
			world.destroyBlock(pos, false);

			if (!EntityUtils.doesPlayerOwn(player, world, pos))
				explode(world, pos);
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public void wasExploded(World world, BlockPos pos, Explosion explosion) {
		if (!world.isClientSide && world.getBlockState(pos).hasProperty(ClaymoreBlock.DEACTIVATED) && !world.getBlockState(pos).getValue(ClaymoreBlock.DEACTIVATED)) {
			if (pos.equals(new BlockPos(explosion.getPosition())))
				return;

			explode(world, pos);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		return getStateForPlacement(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z, ctx.getPlayer());
	}

	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, double hitX, double hitY, double hitZ, PlayerEntity placer) {
		return defaultBlockState().setValue(FACING, placer.getDirection()).setValue(DEACTIVATED, false);
	}

	@Override
	public boolean activateMine(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (state.getValue(DEACTIVATED)) {
			world.setBlockAndUpdate(pos, state.setValue(DEACTIVATED, false));
			return true;
		}

		return false;
	}

	@Override
	public boolean defuseMine(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (!state.getValue(DEACTIVATED)) {
			world.setBlockAndUpdate(pos, state.setValue(DEACTIVATED, true));
			return true;
		}

		return false;
	}

	@Override
	public void explode(World world, BlockPos pos) {
		if (!world.isClientSide) {
			world.destroyBlock(pos, false);
			world.explode((Entity) null, pos.getX(), pos.getY(), pos.getZ(), ConfigHandler.SERVER.smallerMineExplosion.get() ? 1.5F : 3.5F, ConfigHandler.SERVER.shouldSpawnFire.get(), BlockUtils.getExplosionMode());
		}
	}

	@Override
	public void playerWillDestroy(World level, BlockPos pos, BlockState state, PlayerEntity player) {
		//prevents dropping twice the amount of modules when breaking the block in creative mode
		if (player.isCreative()) {
			TileEntity te = level.getBlockEntity(pos);

			if (te instanceof IModuleInventory)
				((IModuleInventory) te).getInventory().clear();
		}

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			TileEntity te = level.getBlockEntity(pos);

			if (te instanceof IModuleInventory)
				((IModuleInventory) te).dropAllModules();

			if (!newState.hasTileEntity())
				level.removeBlockEntity(pos);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext ctx) {
		switch (state.getValue(FACING)) {
			case NORTH:
				return NORTH;
			case EAST:
				return EAST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return WEST;
			default:
				return VoxelShapes.block();
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, DEACTIVATED);
	}

	@Override
	public boolean isActive(World world, BlockPos pos) {
		return !world.getBlockState(pos).getValue(DEACTIVATED);
	}

	@Override
	public boolean explodesWhenInteractedWith() {
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ClaymoreBlockEntity();
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
