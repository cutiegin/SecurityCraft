package net.geforcemods.securitycraft.blocks;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IDoorActivator;
import net.geforcemods.securitycraft.blockentities.SecureRedstoneInterfaceBlockEntity;
import net.geforcemods.securitycraft.network.client.OpenScreen;
import net.geforcemods.securitycraft.network.client.OpenScreen.DataType;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class SecureRedstoneInterfaceBlock extends DisguisableBlock {
	public static final BooleanProperty SENDER = BooleanProperty.create("sender");
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public SecureRedstoneInterfaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(SENDER, false).setValue(FACING, Direction.UP));
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof SecureRedstoneInterfaceBlockEntity be && be.isOwnedBy(player)) {
			if (!level.isClientSide) {
				if (be.isDisabled())
					player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
				else if (be.getOwner().isValidated())
					PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenScreen(DataType.SECURE_REDSTONE_INTERFACE, pos));
				else
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.ownerInvalidated"), ChatFormatting.RED);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return !state.getValue(SENDER);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		if (level.getBlockEntity(pos) instanceof SecureRedstoneInterfaceBlockEntity be && be.isSender() && !be.isDisabled())
			be.refreshPower();
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		if (state.getValue(FACING) == direction)
			return getSignal(state, level, pos, direction);
		else
			return 0;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		if (level.getBlockEntity(pos) instanceof SecureRedstoneInterfaceBlockEntity be)
			return be.getRedstonePowerOutput();
		else
			return 0;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SecureRedstoneInterfaceBlockEntity be) {
			be.disabled.setValue(true); //make sure receivers that update themselves don't check for this one

			if (be.isSender())
				be.tellSimilarReceiversToRefresh();
			else
				be.updateNeighbors();
		}

		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx).setValue(FACING, ctx.getClickedFace());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SecureRedstoneInterfaceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return !level.isClientSide ? createTickerHelper(type, SCContent.SECURE_REDSTONE_INTERFACE_BLOCK_ENTITY.get(), LevelUtils::blockEntityTicker) : null;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(SENDER, FACING, WATERLOGGED);
	}

	public static class DoorActivator implements IDoorActivator {
		private final List<Block> blocks = Arrays.asList(SCContent.SECURE_REDSTONE_INTERFACE.get());

		@Override
		public boolean isPowering(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction, int distance) {
			return !state.getValue(SENDER) && blockEntity instanceof SecureRedstoneInterfaceBlockEntity be && be.isProtectedSignal() && be.getPower() > 0;
		}

		@Override
		public List<Block> getBlocks() {
			return blocks;
		}
	}
}
