package net.geforcemods.securitycraft.network.server;

import java.util.Arrays;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IPasscodeProtected;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CheckPasscode implements CustomPacketPayload {
	public static final Type<CheckPasscode> TYPE = new Type<>(new ResourceLocation(SecurityCraft.MODID, "check_passcode"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CheckPasscode> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public CheckPasscode decode(RegistryFriendlyByteBuf buf) {
			if (buf.readBoolean())
				return new CheckPasscode(buf.readBlockPos(), buf.readUtf(Integer.MAX_VALUE / 4));
			else
				return new CheckPasscode(buf.readVarInt(), buf.readUtf(Integer.MAX_VALUE / 4));
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, CheckPasscode packet) {
			boolean hasPos = packet.pos != null;

			buf.writeBoolean(hasPos);

			if (hasPos)
				buf.writeBlockPos(packet.pos);
			else
				buf.writeVarInt(packet.entityId);

			buf.writeUtf(packet.passcode);
		}
	};
	private BlockPos pos;
	private int entityId;
	private String passcode;

	public CheckPasscode(BlockPos pos, String passcode) {
		this.pos = pos;
		this.passcode = PasscodeUtils.hashPasscodeWithoutSalt(passcode);
	}

	public CheckPasscode(int entityId, String passcode) {
		this.entityId = entityId;
		this.passcode = PasscodeUtils.hashPasscodeWithoutSalt(passcode);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		Player player = ctx.player();
		IPasscodeProtected passcodeProtected = getPasscodeProtected(player.level());

		if (passcodeProtected != null) {
			if (passcodeProtected.isOnCooldown())
				return;

			PasscodeUtils.hashPasscode(passcode, passcodeProtected.getSalt(), p -> {
				if (Arrays.equals(passcodeProtected.getPasscode(), p)) {
					player.closeContainer();
					passcodeProtected.activate(player);
				}
				else
					passcodeProtected.onIncorrectPasscodeEntered(player, passcode);
			});
		}
	}

	private IPasscodeProtected getPasscodeProtected(Level level) {
		if (pos != null) {
			if (level.getBlockEntity(pos) instanceof IPasscodeProtected pp)
				return pp;
		}
		else if (level.getEntity(entityId) instanceof IPasscodeProtected pp)
			return pp;

		return null;
	}
}
