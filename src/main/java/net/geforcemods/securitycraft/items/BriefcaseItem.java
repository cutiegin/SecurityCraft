package net.geforcemods.securitycraft.items;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.misc.SaltData;
import net.geforcemods.securitycraft.screen.ScreenHandler;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BriefcaseItem extends ColorableItem {
	private static final Style GRAY_STYLE = new Style().setColor(TextFormatting.GRAY);

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		extraHandling(stack, world, player);
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void extraHandling(ItemStack stack, World level, EntityPlayer player) {
		if (!level.isRemote) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			player.openGui(SecurityCraft.instance, stack.getTagCompound().hasKey("passcode") ? ScreenHandler.BRIEFCASE_INSERT_CODE_GUI_ID : ScreenHandler.BRIEFCASE_CODE_SETUP_GUI_ID, level, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack briefcase, World world, List<String> tooltip, ITooltipFlag flag) {
		String ownerName = getOwnerName(briefcase);

		if (!ownerName.isEmpty())
			tooltip.add(Utils.localize("tooltip.securitycraft:briefcase.owner", ownerName).setStyle(GRAY_STYLE).getFormattedText());
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		NBTTagCompound tag = super.getNBTShareTag(stack);

		return tag == null ? null : PasscodeUtils.filterPasscodeAndSaltFromTag(tag.copy());
	}

	@Override
	public int getDefaultColor() {
		return 0x333333;
	}

	public static void hashAndSetPasscode(NBTTagCompound briefcaseTag, String passcode, Consumer<byte[]> afterSet) {
		byte[] salt = PasscodeUtils.generateSalt();

		briefcaseTag.setUniqueId("saltKey", SaltData.putSalt(salt));
		PasscodeUtils.hashPasscode(passcode, salt, p -> {
			briefcaseTag.setString("passcode", PasscodeUtils.bytesToString(p));
			afterSet.accept(p);
		});
	}

	public static void checkPasscode(EntityPlayerMP player, ItemStack briefcase, String incomingCode, String briefcaseCode, NBTTagCompound tag) {
		UUID saltKey = tag.hasUniqueId("saltKey") ? tag.getUniqueId("saltKey") : null;
		byte[] salt = SaltData.getSalt(saltKey);

		if (salt == null) { //If no salt key or no salt associated with the given key can be found, a new passcode needs to be set
			PasscodeUtils.filterPasscodeAndSaltFromTag(tag);
			return;
		}

		PasscodeUtils.hashPasscode(incomingCode, salt, p -> {
			if (Arrays.equals(PasscodeUtils.stringToBytes(briefcaseCode), p)) {
				if (!tag.hasKey("owner")) { //If the briefcase doesn't have an owner (that usually gets set when assigning a new passcode), set the player that first enters the correct passcode as the owner
					tag.setString("owner", player.getName());
					tag.setString("ownerUUID", player.getUniqueID().toString());
				}

				player.openGui(SecurityCraft.instance, ScreenHandler.BRIEFCASE_GUI_ID, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
			}
		});
	}

	public static boolean isOwnedBy(ItemStack briefcase, EntityPlayer player) {
		if (!briefcase.hasTagCompound())
			return true;

		String ownerName = getOwnerName(briefcase);
		String ownerUUID = getOwnerUUID(briefcase);

		return ownerName.isEmpty() || ownerUUID.equals(player.getUniqueID().toString()) || (ownerUUID.equals("ownerUUID") && ownerName.equals(player.getName()));
	}

	public static String getOwnerName(ItemStack briefcase) {
		return briefcase.hasTagCompound() ? briefcase.getTagCompound().getString("owner") : "";
	}

	public static String getOwnerUUID(ItemStack briefcase) {
		return briefcase.hasTagCompound() ? briefcase.getTagCompound().getString("ownerUUID") : "";
	}
}
