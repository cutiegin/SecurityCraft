package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.AlarmBlock;
import net.geforcemods.securitycraft.blocks.OldLitAlarmBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

public class AlarmBlockEntity extends CustomizableBlockEntity implements ITickable {
	public IntOption range = new IntOption(this::getPos, "range", 17, 0, ConfigHandler.maxAlarmRange, 1, true);
	private IntOption delay = new IntOption(this::getPos, "delay", 2, 1, 30, 1, true);
	private int cooldown = 0;
	private boolean isPowered = false;

	@Override
	public void update() {
		//convert the old lit alarm block to the old unlit alarm block, which now has a LIT property
		if (getBlockType() == SCContent.alarmLit) {
			world.setBlockState(pos, SCContent.alarm.getDefaultState().withProperty(AlarmBlock.FACING, world.getBlockState(pos).getValue(OldLitAlarmBlock.FACING)).withProperty(AlarmBlock.LIT, false));

			AlarmBlockEntity newTe = (AlarmBlockEntity) world.getTileEntity(pos);

			newTe.getOwner().set(getOwner().getUUID(), getOwner().getName());
			newTe.range.copy(range);
			newTe.delay.copy(delay);
			newTe.setPowered(false);
			invalidate();
			return;
		}

		if (!world.isRemote && isPowered && --cooldown <= 0) {
			double rangeSqr = Math.pow(range.get(), 2);

			for (EntityPlayerMP player : world.getPlayers(EntityPlayerMP.class, p -> p.getPosition().distanceSq(pos) <= rangeSqr)) {
				float volume = (float) (1.0F - ((player.getPosition().distanceSq(pos)) / rangeSqr));

				player.connection.sendPacket(new SPacketSoundEffect(SCSounds.ALARM.event, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), volume, 1.0F));
			}

			setCooldown(delay.get() * 20);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("cooldown", cooldown);
		tag.setBoolean("isPowered", isPowered);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		cooldown = tag.getInteger("cooldown");
		isPowered = tag.getBoolean("isPowered");
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public boolean isPowered() {
		return isPowered;
	}

	public void setPowered(boolean isPowered) {
		this.isPowered = isPowered;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				range, delay
		};
	}
}
