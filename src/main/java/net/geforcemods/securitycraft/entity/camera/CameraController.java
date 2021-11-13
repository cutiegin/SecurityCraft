package net.geforcemods.securitycraft.entity.camera;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.server.DismountCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid=SecurityCraft.MODID, value=Dist.CLIENT)
public class CameraController
{
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			Entity cameraEntity = Minecraft.getInstance().cameraEntity;

			if(cameraEntity instanceof SecurityCamera cam)
			{
				Options options = Minecraft.getInstance().options;

				if(options.keyShift.isDown())
					dismount();

				if(options.keyUp.isDown())
					moveViewUp(cam);

				if(options.keyDown.isDown())
					moveViewDown(cam);

				if(options.keyLeft.isDown())
					moveViewHorizontally(cam, cam.getYRot(), cam.getXRot(), cam.getYRot() - (float)cam.cameraSpeed);

				if(options.keyRight.isDown())
					moveViewHorizontally(cam, cam.getYRot(), cam.getXRot(), cam.getYRot() + (float)cam.cameraSpeed);

				if(KeyBindings.cameraZoomIn.isDown())
					zoomIn(cam);
				else if(KeyBindings.cameraZoomOut.isDown())
					zoomOut(cam);
				else
					cam.zooming = false;

				if(KeyBindings.cameraEmitRedstone.consumeClick())
					emitRedstone(cam);

				if(KeyBindings.cameraActivateNightVision.consumeClick())
					giveNightVision(cam);

				//update other players with the head rotation
				LocalPlayer player = Minecraft.getInstance().player;
				double yRotChange = player.getYRot() - player.yRotLast;
				double xRotChange = player.getXRot() - player.xRotLast;

				if(yRotChange != 0.0D || xRotChange != 0.0D)
					player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.isOnGround()));
			}
		}
	}

	private static void dismount()
	{
		Minecraft.getInstance().setCameraEntity(null);
		SecurityCraft.channel.sendToServer(new DismountCamera());
		OverlayRegistry.enableOverlay(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, true);
		OverlayRegistry.enableOverlay(ClientHandler.cameraOverlay, false);
		OverlayRegistry.enableOverlay(ClientHandler.hotbarBindOverlay, true);
	}

	public static void moveViewUp(SecurityCamera cam)
	{
		float next = cam.getXRot() - (float)cam.cameraSpeed;

		if(cam.isCameraDown())
		{
			if(next > 40F)
				cam.setRotation(cam.getYRot(), next);
		}
		else if(next > -25F)
			cam.setRotation(cam.getYRot(), next);
	}

	public static void moveViewDown(SecurityCamera cam)
	{
		float next = cam.getXRot() + (float)cam.cameraSpeed;

		if(cam.isCameraDown())
		{
			if(next < 100F)
				cam.setRotation(cam.getYRot(), next);
		}
		else if(next < 60F)
			cam.setRotation(cam.getYRot(), next);
	}

	public static void moveViewHorizontally(SecurityCamera cam, float yRot, float xRot, float next)
	{
		BlockState state = cam.level.getBlockState(cam.blockPosition());

		if(state.hasProperty(SecurityCameraBlock.FACING))
		{
			if(next < 0)
				next += 360;

			boolean shouldSetRotation = switch(state.getValue(SecurityCameraBlock.FACING)) {
				case NORTH -> next > 90F && next < 270F;
				case SOUTH -> next > 270F || next < 90F;
				case EAST -> next > 180F && next < 360F;
				case WEST -> next > 0F && next < 180F;
				case DOWN -> true;
				default -> false;
			};

			if(shouldSetRotation)
				cam.setRotation(next, xRot);
		}
	}

	public static void zoomIn(SecurityCamera cam)
	{
		if(!cam.zooming)
			Minecraft.getInstance().level.playLocalSound(cam.blockPosition(), SCSounds.CAMERAZOOMIN.event, SoundSource.BLOCKS, 1.0F, 1.0F, true);

		cam.zooming = true;
		cam.zoomAmount = Math.max(cam.zoomAmount - 0.1F, 0.1F);
	}

	public static void zoomOut(SecurityCamera cam)
	{
		if(!cam.zooming)
			Minecraft.getInstance().level.playLocalSound(cam.blockPosition(), SCSounds.CAMERAZOOMIN.event, SoundSource.BLOCKS, 1.0F, 1.0F, true);

		cam.zooming = true;
		cam.zoomAmount = Math.min(cam.zoomAmount + 0.1F, 1.4F);
	}

	public static void emitRedstone(SecurityCamera cam)
	{
		if(cam.redstoneCooldown == 0)
		{
			cam.toggleRedstonePower();
			cam.redstoneCooldown = 30;
		}
	}

	public static void giveNightVision(SecurityCamera cam)
	{
		if(cam.toggleNightVisionCooldown == 0)
			cam.toggleNightVision();
	}
}
