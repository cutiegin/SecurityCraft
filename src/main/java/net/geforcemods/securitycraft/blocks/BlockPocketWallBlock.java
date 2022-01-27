package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.tileentity.BlockPocketTileEntity;
import net.geforcemods.securitycraft.util.IBlockPocket;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockPocketWallBlock extends OwnableBlock implements IBlockPocket {
	public static final BooleanProperty SEE_THROUGH = BooleanProperty.create("see_through");
	public static final BooleanProperty SOLID = BooleanProperty.create("solid");

	public BlockPocketWallBlock(Block.Properties properties) {
		super(properties);

		registerDefaultState(stateDefinition.any().setValue(SEE_THROUGH, true).setValue(SOLID, false));
	}

	public static boolean causesSuffocation(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getValue(SOLID);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
		if (!state.getValue(SOLID) && ctx instanceof EntitySelectionContext) {
			Entity entity = ((EntitySelectionContext) ctx).getEntity();

			if (entity instanceof PlayerEntity) {
				TileEntity te1 = world.getBlockEntity(pos);

				if (te1 instanceof BlockPocketTileEntity) {
					BlockPocketTileEntity te = (BlockPocketTileEntity) te1;

					if (te.getManager() == null)
						return VoxelShapes.empty();

					if (ModuleUtils.isAllowed(te.getManager(), entity))
						return VoxelShapes.empty();
					else if (!te.getOwner().isOwner((PlayerEntity) entity))
						return VoxelShapes.block();
					else
						return VoxelShapes.empty();
				}
			}
		}

		return VoxelShapes.block();
	}

	@Override
	public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return state.getValue(SEE_THROUGH) && adjacentBlockState.getBlock() == SCContent.BLOCK_POCKET_WALL.get();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).setValue(SEE_THROUGH, true);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(SEE_THROUGH, SOLID);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BlockPocketTileEntity();
	}
}
