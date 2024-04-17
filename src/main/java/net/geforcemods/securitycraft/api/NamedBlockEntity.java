package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NamedBlockEntity extends OwnableBlockEntity implements Nameable {
	private Component customName;

	public NamedBlockEntity(BlockPos pos, BlockState state) {
		this(SCContent.ABSTRACT_BLOCK_ENTITY.get(), pos, state);
	}

	public NamedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.saveAdditional(tag, lookupProvider);

		if (customName != null)
			tag.putString("customName", customName.getString());
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.loadAdditional(tag, lookupProvider);

		if (tag.contains("customName")) {
			String name = tag.getString("customName");

			if (!name.isEmpty() && !name.equals("name"))
				customName = Component.literal(name);
		}
	}

	@Override
	public Component getName() {
		return hasCustomName() ? getCustomName() : getDefaultName();
	}

	@Override
	public boolean hasCustomName() {
		Component name = getCustomName();

		return name != null && !Component.empty().equals(name) && !getDefaultName().equals(name);
	}

	@Override
	public Component getCustomName() {
		return customName;
	}

	public void setCustomName(Component customName) {
		this.customName = customName;
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public Component getDefaultName() {
		return Utils.localize(getBlockState().getBlock().getDescriptionId());
	}
}
