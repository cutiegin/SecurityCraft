package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class OwnableFenceGateBlock extends BlockFenceGate implements ITileEntityProvider {
	protected final SoundEvent openSound;
	protected final SoundEvent closeSound;

	public OwnableFenceGateBlock(EnumType type, SoundEvent openSound, SoundEvent closeSound) {
		super(type);
		this.openSound = openSound;
		this.closeSound = closeSound;
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		return Float.MAX_VALUE;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return Float.MAX_VALUE;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (placer instanceof EntityPlayer)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (EntityPlayer) placer));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int eventID, int eventParam) {
		TileEntity te = world.getTileEntity(pos);

		return te != null && te.receiveClientEvent(eventID, eventParam);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			boolean isPoweredSCBlock = BlockUtils.hasActiveSCBlockNextTo(world, pos);

			if (isPoweredSCBlock || block.getDefaultState().canProvidePower()) {
				if (isPoweredSCBlock && !state.getValue(OPEN) && !state.getValue(POWERED)) {
					world.setBlockState(pos, state.withProperty(OPEN, true).withProperty(POWERED, true), 2);
					world.playSound(null, pos, openSound, SoundCategory.BLOCKS, 1.0F, SecurityCraft.RANDOM.nextFloat() * 0.1F + 0.9F);
				}
				else if (!isPoweredSCBlock && state.getValue(OPEN) && state.getValue(POWERED)) {
					world.setBlockState(pos, state.withProperty(OPEN, false).withProperty(POWERED, false), 2);
					world.playSound(null, pos, closeSound, SoundCategory.BLOCKS, 1.0F, SecurityCraft.RANDOM.nextFloat() * 0.1F + 0.9F);
				}
				else if (isPoweredSCBlock != state.getValue(POWERED))
					world.setBlockState(pos, state.withProperty(POWERED, isPoweredSCBlock), 2);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new OwnableBlockEntity();
	}
}
