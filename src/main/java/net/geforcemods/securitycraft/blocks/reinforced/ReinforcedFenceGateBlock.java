package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.AllowlistOnlyBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableFenceGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReinforcedFenceGateBlock extends OwnableFenceGateBlock implements IReinforcedBlock {
	private final Block vanillaBlock;

	public ReinforcedFenceGateBlock(EnumType type, Block vanillaBlock) {
		super(type, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundEvents.BLOCK_FENCE_GATE_CLOSE);
		this.vanillaBlock = vanillaBlock;
		setSoundType(SoundType.WOOD);
	}

	@Override
	public boolean onBlockActivated(World level, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = level.getTileEntity(pos);

		if (te instanceof AllowlistOnlyBlockEntity) {
			AllowlistOnlyBlockEntity be = (AllowlistOnlyBlockEntity) te;

			//only allow the owner or players on the allowlist to access a reinforced fence gate
			if (be.isOwnedBy(player) || be.isAllowed(player)) {
				if ((state.getValue(OPEN))) {
					state = state.withProperty(OPEN, false);
					level.setBlockState(pos, state, 10);
				}
				else {
					EnumFacing playerRotation = EnumFacing.fromAngle(player.rotationYaw);

					if (state.getValue(FACING) == playerRotation.getOpposite())
						state = state.withProperty(FACING, playerRotation);

					state = state.withProperty(OPEN, true);
					level.setBlockState(pos, state, 10);
				}

				boolean isOpen = state.getValue(OPEN);

				level.playSound(null, pos, isOpen ? openSound : closeSound, SoundCategory.BLOCKS, 1.0F, SecurityCraft.RANDOM.nextFloat() * 0.1F + 0.9F);
			}
		}

		return true;
	}

	@Override
	public boolean eventReceived(IBlockState state, World level, BlockPos pos, int id, int param) {
		TileEntity be = level.getTileEntity(pos);

		return be != null && be.receiveClientEvent(id, param);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new AllowlistOnlyBlockEntity();
	}

	@Override
	public List<Block> getVanillaBlocks() {
		return Arrays.asList(vanillaBlock);
	}

	@Override
	public int getAmount() {
		return 1;
	}
}
