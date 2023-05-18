package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetBriefcaseOwner {
	private String passcode;

	public SetBriefcaseOwner() {}

	public SetBriefcaseOwner(String passcode) {
		this.passcode = passcode;
	}

	public SetBriefcaseOwner(PacketBuffer buf) {
		passcode = buf.readUtf();
	}

	public void encode(PacketBuffer buf) {
		buf.writeUtf(passcode);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		PlayerEntity player = ctx.get().getSender();
		ItemStack stack = PlayerUtils.getSelectedItemStack(player, SCContent.BRIEFCASE.get());

		if (!stack.isEmpty()) {
			CompoundNBT tag = stack.getOrCreateTag();

			if (!tag.contains("owner")) {
				tag.putString("owner", player.getName().getString());
				tag.putString("ownerUUID", player.getUUID().toString());
			}

			if (!tag.contains("passcode") && passcode.matches("[0-9]{4}"))
				tag.putString("passcode", passcode);
		}
	}
}
