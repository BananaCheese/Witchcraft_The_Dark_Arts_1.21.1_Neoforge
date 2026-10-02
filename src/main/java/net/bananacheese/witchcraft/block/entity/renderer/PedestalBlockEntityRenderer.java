package net.bananacheese.witchcraft.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    private static final float ROTATION_SPEED = 1.0F;

    public PedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            PedestalBlockEntity pedestalBlockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLight,
            int packedOverlay) {

        ItemStack stack = pedestalBlockEntity.getHeldItem();

        if (stack.isEmpty()) {
            return;
        }

        Level level = pedestalBlockEntity.getLevel();

        if (level == null) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float rotation =
                (level.getGameTime() + partialTick) * ROTATION_SPEED;

        poseStack.pushPose();

        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GUI,
                getLightLevel(level, pedestalBlockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY,
                poseStack,
                multiBufferSource,
                level,
                1);

        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        return LightTexture.pack(blockLight, skyLight);
    }
}