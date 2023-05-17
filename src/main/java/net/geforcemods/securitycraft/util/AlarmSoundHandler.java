package net.geforcemods.securitycraft.util;

import java.util.HashMap;
import java.util.Map;

import net.geforcemods.securitycraft.blockentities.AlarmBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

public final class AlarmSoundHandler {
	private static final Map<AlarmBlockEntity, SoundInstance> SOUND_STORAGE = new HashMap<>();

	private AlarmSoundHandler() {}

	public static void playSound(AlarmBlockEntity be, Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
		Minecraft mc = Minecraft.getInstance();
		PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(mc.player, sound, source, volume, pitch);

		if (event.isCanceled() || event.getSound() == null)
			return;

		SimpleSoundInstance soundInstance = new SimpleSoundInstance(event.getSound(), event.getCategory(), event.getVolume(), event.getPitch(), x, y, z);

		stopCurrentSound(be);
		mc.getSoundManager().play(soundInstance);
		SOUND_STORAGE.put(be, soundInstance);
	}

	public static void stopCurrentSound(AlarmBlockEntity be) {
		SoundInstance soundInstance = SOUND_STORAGE.remove(be);

		if (soundInstance != null)
			Minecraft.getInstance().getSoundManager().stop(soundInstance);
	}
}