package net.geforcemods.securitycraft.blocks;

import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordConvertible;
import net.geforcemods.securitycraft.blockentities.AbstractKeypadFurnaceBlockEntity;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public abstract class AbstractKeypadFurnaceBlock extends DisguisableBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final VoxelShape NORTH_OPEN = Shapes.joinUnoptimized(Shapes.or(Shapes.or(Block.box(0, 0, 3, 16, 16, 16), Block.box(1, 1, 2, 15, 2, 3)), Shapes.joinUnoptimized(Block.box(4, 1, 0, 12, 2, 2), Block.box(5, 1, 1, 11, 2, 2), BooleanOp.ONLY_FIRST)), Block.box(1, 2, 3, 15, 15, 4), BooleanOp.ONLY_FIRST);
	private static final VoxelShape NORTH_CLOSED = Shapes.or(Shapes.or(Block.box(0, 0, 3, 16, 16, 16), Block.box(1, 1, 2, 15, 15, 3)), Shapes.joinUnoptimized(Block.box(4, 14, 0, 12, 15, 2), Block.box(5, 14, 1, 11, 15, 2), BooleanOp.ONLY_FIRST));
	private static final VoxelShape EAST_OPEN = Shapes.joinUnoptimized(Shapes.or(Shapes.or(Block.box(0, 0, 0, 13, 16, 16), Block.box(13, 1, 1, 14, 2, 15)), Shapes.joinUnoptimized(Block.box(14, 1, 4, 16, 2, 12), Block.box(14, 1, 5, 15, 2, 11), BooleanOp.ONLY_FIRST)), Block.box(12, 2, 1, 13, 15, 15), BooleanOp.ONLY_FIRST);
	private static final VoxelShape EAST_CLOSED = Shapes.or(Shapes.or(Block.box(0, 0, 0, 13, 16, 16), Block.box(13, 1, 1, 14, 15, 15)), Shapes.joinUnoptimized(Block.box(14, 14, 4, 16, 15, 12), Block.box(14, 14, 5, 15, 15, 11), BooleanOp.ONLY_FIRST));
	private static final VoxelShape SOUTH_OPEN = Shapes.joinUnoptimized(Shapes.or(Shapes.or(Block.box(0, 0, 0, 16, 16, 13), Block.box(1, 1, 13, 15, 2, 14)), Shapes.joinUnoptimized(Block.box(4, 1, 14, 12, 2, 16), Block.box(5, 1, 14, 11, 2, 15), BooleanOp.ONLY_FIRST)), Block.box(1, 2, 12, 15, 15, 13), BooleanOp.ONLY_FIRST);
	private static final VoxelShape SOUTH_CLOSED = Shapes.or(Shapes.or(Block.box(0, 0, 0, 16, 16, 13), Block.box(1, 1, 13, 15, 15, 14)), Shapes.joinUnoptimized(Block.box(4, 14, 14, 12, 15, 16), Block.box(5, 14, 14, 11, 15, 15), BooleanOp.ONLY_FIRST));
	private static final VoxelShape WEST_OPEN = Shapes.joinUnoptimized(Shapes.or(Shapes.or(Block.box(3, 0, 0, 16, 16, 16), Block.box(2, 1, 1, 3, 2, 15)), Shapes.joinUnoptimized(Block.box(0, 1, 4, 2, 2, 12), Block.box(1, 1, 5, 2, 2, 11), BooleanOp.ONLY_FIRST)), Block.box(3, 2, 1, 4, 15, 15), BooleanOp.ONLY_FIRST);
	private static final VoxelShape WEST_CLOSED = Shapes.or(Shapes.or(Block.box(3, 0, 0, 16, 16, 16), Block.box(2, 1, 1, 3, 15, 15)), Shapes.joinUnoptimized(Block.box(0, 14, 4, 2, 15, 12), Block.box(1, 14, 5, 2, 15, 11), BooleanOp.ONLY_FIRST));

	public AbstractKeypadFurnaceBlock(Block.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(LIT, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		BlockState disguisedState = getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getShape(level, pos, ctx);
		else
			return switch (state.getValue(FACING)) {
				case NORTH -> state.getValue(OPEN) ? NORTH_OPEN : NORTH_CLOSED;
				case EAST -> state.getValue(OPEN) ? EAST_OPEN : EAST_CLOSED;
				case SOUTH -> state.getValue(OPEN) ? SOUTH_OPEN : SOUTH_CLOSED;
				case WEST -> state.getValue(OPEN) ? WEST_OPEN : WEST_CLOSED;
				default -> Shapes.block();
			};
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!(newState.getBlock() instanceof AbstractKeypadFurnaceBlock)) {
			if (level.getBlockEntity(pos) instanceof Container container) {
				Containers.dropContents(level, pos, container);
				level.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide) {
			AbstractKeypadFurnaceBlockEntity be = (AbstractKeypadFurnaceBlockEntity) level.getBlockEntity(pos);

			if (ModuleUtils.isDenied(be, player)) {
				if (be.sendsMessages())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
			}
			else if (ModuleUtils.isAllowed(be, player)) {
				if (be.sendsMessages())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

				activate(be, state, level, pos, player);
			}
			else if (!PlayerUtils.isHoldingItem(player, SCContent.CODEBREAKER, hand))
				be.openPasswordGUI(player);
		}

		return InteractionResult.SUCCESS;
	}

	public void activate(AbstractKeypadFurnaceBlockEntity be, BlockState state, Level level, BlockPos pos, Player player) {
		if (!state.getValue(AbstractKeypadFurnaceBlock.OPEN))
			level.setBlockAndUpdate(pos, state.setValue(AbstractKeypadFurnaceBlock.OPEN, true));

		if (player instanceof ServerPlayer serverPlayer) {
			level.levelEvent((Player) null, 1006, pos, 0);
			NetworkHooks.openGui(serverPlayer, be, pos);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return getStateForPlacement(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z, ctx.getPlayer());
	}

	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, double hitX, double hitY, double hitZ, Player placer) {
		return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite()).setValue(OPEN, false).setValue(LIT, false);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
		if (state.getValue(OPEN) && state.getValue(LIT) && getDisguisedStateOrDefault(state, level, pos).getBlock() != this) {
			double x = pos.getX() + 0.5D;
			double y = pos.getY();
			double z = pos.getZ() + 0.5D;

			if (rand.nextDouble() < 0.1D)
				level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);

			Direction direction = state.getValue(FACING);
			Axis axis = direction.getAxis();
			double randomNumber = rand.nextDouble() * 0.6D - 0.3D;
			double xOffset = axis == Axis.X ? direction.getStepX() * 0.52D : randomNumber;
			double yOffset = rand.nextDouble() * 6.0D / 16.0D;
			double zOffset = axis == Axis.Z ? direction.getStepZ() * 0.52D : randomNumber;

			level.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
			level.addParticle(ParticleTypes.FLAME, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN, LIT);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	public class Convertible implements IPasswordConvertible {
		private final Block originalBlock;

		public Convertible(Block originalBlock) {
			this.originalBlock = originalBlock;
		}

		@Override
		public Block getOriginalBlock() {
			return originalBlock;
		}

		@Override
		public boolean convert(Player player, Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			Direction facing = state.getValue(FACING);
			boolean lit = state.getValue(LIT);
			AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) level.getBlockEntity(pos);
			CompoundTag tag = furnace.saveWithFullMetadata();

			furnace.clearContent();
			level.setBlockAndUpdate(pos, AbstractKeypadFurnaceBlock.this.defaultBlockState().setValue(FACING, facing).setValue(OPEN, false).setValue(LIT, lit));
			((AbstractKeypadFurnaceBlockEntity) level.getBlockEntity(pos)).load(tag);
			((IOwnable) level.getBlockEntity(pos)).setOwner(player.getUUID().toString(), player.getName().getString());
			return true;
		}
	}
}
