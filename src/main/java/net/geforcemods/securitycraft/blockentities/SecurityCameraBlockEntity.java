package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IEMPAffectedBE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.DoubleOption;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;

public class SecurityCameraBlockEntity extends CustomizableBlockEntity implements ITickableTileEntity, IEMPAffectedBE {
	public double cameraRotation = 0.0D;
	public double oCameraRotation = 0.0D;
	public boolean addToRotation = true;
	public boolean down = false, downSet = false;
	private int playersViewing = 0;
	private boolean shutDown = false;
	private DoubleOption rotationSpeedOption = new DoubleOption(this::getBlockPos, "rotationSpeed", 0.018D, 0.01D, 0.025D, 0.001D, true);
	private BooleanOption shouldRotateOption = new BooleanOption("shouldRotate", true);
	private DoubleOption customRotationOption = new DoubleOption(this::getBlockPos, "customRotation", cameraRotation, 1.55D, -1.55D, rotationSpeedOption.get(), true);
	private DisabledOption disabled = new DisabledOption(false);

	public SecurityCameraBlockEntity() {
		super(SCContent.SECURITY_CAMERA_BLOCK_ENTITY.get());
	}

	@Override
	public void tick() {
		if (!downSet) {
			down = getBlockState().getValue(SecurityCameraBlock.FACING) == Direction.DOWN;
			downSet = true;
		}

		if (!shutDown) {
			oCameraRotation = cameraRotation;

			if (!shouldRotateOption.get()) {
				cameraRotation = customRotationOption.get();
				return;
			}

			if (addToRotation && cameraRotation <= 1.55F)
				cameraRotation += rotationSpeedOption.get();
			else
				addToRotation = false;

			if (!addToRotation && cameraRotation >= -1.55F)
				cameraRotation -= rotationSpeedOption.get();
			else
				addToRotation = true;
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);
		tag.putBoolean("shutDown", shutDown);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);
		shutDown = tag.getBoolean("shutDown");
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.REDSTONE, ModuleType.ALLOWLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				rotationSpeedOption, shouldRotateOption, customRotationOption, disabled
		};
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);

		if (module == ModuleType.REDSTONE)
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SecurityCameraBlock.POWERED, false));
	}

	@Override
	public void onOptionChanged(Option<?> option) {
		if (option.getName().equals("disabled")) {
			//make players stop viewing the camera when it's disabled
			if (!level.isClientSide && ((BooleanOption) option).get()) {
				for (ServerPlayerEntity player : ((ServerWorld) level).players()) {
					if (player.getCamera() instanceof SecurityCamera) {
						SecurityCamera camera = (SecurityCamera) player.getCamera();

						if (camera.blockPosition().equals(worldPosition))
							camera.stopViewing(player);
					}
				}
			}
		}
	}

	@Override
	public void shutDown() {
		BlockState state = level.getBlockState(worldPosition);

		IEMPAffectedBE.super.shutDown();

		if (state.getBlock() == SCContent.SECURITY_CAMERA.get() && state.getValue(SecurityCameraBlock.POWERED))
			level.setBlockAndUpdate(worldPosition, state.setValue(SecurityCameraBlock.POWERED, false));
	}

	@Override
	public boolean isShutDown() {
		return shutDown;
	}

	@Override
	public void setShutDown(boolean shutDown) {
		this.shutDown = shutDown;
	}

	public void startViewing() {
		if (playersViewing++ == 0)
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SecurityCameraBlock.BEING_VIEWED, true));
	}

	public void stopViewing() {
		if (--playersViewing == 0)
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SecurityCameraBlock.BEING_VIEWED, false));
	}

	public boolean isDisabled() {
		return disabled.get();
	}
}
