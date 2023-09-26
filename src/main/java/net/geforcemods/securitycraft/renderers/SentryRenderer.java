package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.models.SentryModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SentryRenderer extends EntityRenderer<Sentry> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SecurityCraft.MODID + ":textures/entity/sentry.png");
	private static final SentryModel MODEL = new SentryModel();

	public SentryRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	public void render(Sentry entity, float entityYaw, float partialTicks, MatrixStack pose, IRenderTypeBuffer buffer, int pPackedLight) {
		IVertexBuilder builder = buffer.getBuffer(RenderType.entitySolid(getTextureLocation(entity)));

		pose.pushPose();
		pose.translate(0.0D, 1.5D, 0.0D);
		pose.scale(-1, -1, 1); //rotate model rightside up
		Minecraft.getInstance().textureManager.bind(getTextureLocation(entity));
		MODEL.renderBase(pose, builder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		pose.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.getOriginalHeadRotation(), entity.getHeadRotation())));
		pose.translate(0.0F, entity.getHeadYTranslation(partialTicks), 0.0F);
		MODEL.renderToBuffer(pose, builder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		pose.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(Sentry entity) {
		return TEXTURE;
	}
}
