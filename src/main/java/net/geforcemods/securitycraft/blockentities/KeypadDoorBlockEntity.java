package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;
import java.util.function.Consumer;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.INameSetter;
import net.geforcemods.securitycraft.api.IPasscodeProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.blocks.KeypadDoorBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class KeypadDoorBlockEntity extends SpecialDoorBlockEntity implements IPasscodeProtected {
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption(this::getBlockPos);
	private long cooldownEnd = 0;
	private byte[] passcode;
	private UUID saltKey;

	public KeypadDoorBlockEntity() {
		super(SCContent.KEYPAD_DOOR_BLOCK_ENTITY.get());
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);

		if (saltKey != null)
			tag.putUUID("saltKey", saltKey);

		if (passcode != null)
			tag.putString("passcode", PasscodeUtils.bytesToString(passcode));

		long cooldownLeft = getCooldownEnd() - System.currentTimeMillis();

		tag.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);

		loadSaltKey(tag);
		loadPasscode(tag);
		cooldownEnd = System.currentTimeMillis() + tag.getLong("cooldownLeft");
	}

	@Override
	public void activate(PlayerEntity player) {
		if (!level.isClientSide && getBlockState().getBlock() instanceof KeypadDoorBlock)
			((KeypadDoorBlock) getBlockState().getBlock()).activate(getBlockState(), level, worldPosition, getSignalLength());
	}

	@Override
	public boolean shouldAttemptCodebreak(BlockState state, PlayerEntity player) {
		if (isDisabled()) {
			player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			return false;
		}

		return !state.getValue(DoorBlock.POWERED) && IPasscodeProtected.super.shouldAttemptCodebreak(state, player);
	}

	@Override
	public byte[] getPasscode() {
		return passcode == null || passcode.length == 0 ? null : passcode;
	}

	@Override
	public void setPasscode(byte[] passcode) {
		this.passcode = passcode;
		setChanged();
	}

	@Override
	public UUID getSaltKey() {
		return saltKey;
	}

	@Override
	public void setSaltKey(UUID saltKey) {
		this.saltKey = saltKey;
		setChanged();
	}

	@Override
	public void startCooldown() {
		long start = System.currentTimeMillis();

		startCooldown(start);
		runForOtherHalf(otherHalf -> otherHalf.startCooldown(start));
	}

	public void startCooldown(long start) {
		if (!isOnCooldown()) {
			cooldownEnd = start + smartModuleCooldown.get() * 50;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
			setChanged();
		}
	}

	@Override
	public long getCooldownEnd() {
		return cooldownEnd;
	}

	@Override
	public boolean isOnCooldown() {
		return System.currentTimeMillis() < getCooldownEnd();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART, ModuleType.HARMING
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage, signalLength, disabled, smartModuleCooldown
		};
	}

	@Override
	public int defaultSignalLength() {
		return 60;
	}

	@Override
	public void setCustomName(ITextComponent customName) {
		super.setCustomName(customName);

		if (getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			((INameSetter) level.getBlockEntity(worldPosition.above())).setCustomName(customName);
	}

	public void runForOtherHalf(Consumer<KeypadDoorBlockEntity> action) {
		TileEntity be = null;

		if (level == null) //Happens when loading the BE, in that case running the same code for the other half is unnecessary
			return;

		if (getBlockState().getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
			be = level.getBlockEntity(worldPosition.above());
		else if (getBlockState().getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
			be = level.getBlockEntity(worldPosition.below());

		if (be instanceof KeypadDoorBlockEntity)
			action.accept((KeypadDoorBlockEntity) be);
	}
}
