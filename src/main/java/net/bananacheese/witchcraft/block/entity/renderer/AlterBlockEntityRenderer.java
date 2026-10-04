package net.bananacheese.witchcraft.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class AlterBlockEntityRenderer implements BlockEntityRenderer<AlterBlockEntity> {
    private static final float ROTATION_SPEED = 1.0f;

    private static final float FLUID_MIN_X = 0.22f;
    private static final float FLUID_MAX_X = 0.78f;
    private static final float FLUID_MIN_Z = 0.22f;
    private static final float FLUID_MAX_Z = 0.78f;
    private static final float FLUID_BOTTOM = 0.80f;
    private static final float FLUID_MAX_HEIGHT = 0.12f;

    public AlterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            AlterBlockEntity alterBlockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {

        renderFluid(alterBlockEntity, poseStack, buffer, packedLight);
        renderHeldItem(alterBlockEntity, partialTick, poseStack, buffer);
    }

    private void renderFluid(
            AlterBlockEntity alterBlockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {

        FluidStack fluidStack = alterBlockEntity.getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }

        Level level = alterBlockEntity.getLevel();
        if (level == null) {
            return;
        }

        BlockPos pos = alterBlockEntity.getBlockPos();

        IClientFluidTypeExtensions fluidExtensions =
                IClientFluidTypeExtensions.of(fluidStack.getFluid());

        ResourceLocation stillTexture = fluidExtensions.getStillTexture(fluidStack);
        if (stillTexture == null) {
            return;
        }

        FluidState fluidState = fluidStack.getFluid().defaultFluidState();

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        int tintColor = fluidExtensions.getTintColor(fluidState, level, pos);

        float fillRatio = alterBlockEntity.getFluidAmount()
                / (float) AlterBlockEntity.MAX_FLUID;

        float height = FLUID_BOTTOM + FLUID_MAX_HEIGHT * fillRatio;

        VertexConsumer consumer =
                buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluidState));

        // Top surface.
        drawQuad(
                consumer, poseStack,
                FLUID_MIN_X, height, FLUID_MIN_Z,
                FLUID_MAX_X, height, FLUID_MAX_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor);

        // Four visible sides.
        drawQuad(
                consumer, poseStack,
                FLUID_MIN_X, FLUID_BOTTOM, FLUID_MIN_Z,
                FLUID_MAX_X, height, FLUID_MIN_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor);

        drawQuad(
                consumer, poseStack,
                FLUID_MAX_X, FLUID_BOTTOM, FLUID_MAX_Z,
                FLUID_MIN_X, height, FLUID_MAX_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor);

        drawQuad(
                consumer, poseStack,
                FLUID_MAX_X, FLUID_BOTTOM, FLUID_MIN_Z,
                FLUID_MAX_X, height, FLUID_MAX_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor);

        drawQuad(
                consumer, poseStack,
                FLUID_MIN_X, FLUID_BOTTOM, FLUID_MAX_Z,
                FLUID_MIN_X, height, FLUID_MIN_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor);
    }

    private void renderHeldItem(
            AlterBlockEntity alterBlockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource) {

        ItemStack stack = alterBlockEntity.getHeldItem();

        if (stack.isEmpty()) {
            return;
        }

        Level level = alterBlockEntity.getLevel();

        if (level == null) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float rotation =
                (level.getGameTime() + partialTick) * ROTATION_SPEED;

        poseStack.pushPose();

        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GUI,
                getLightLevel(level, alterBlockEntity.getBlockPos()),
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

    private static void drawVertex(
            VertexConsumer consumer,
            PoseStack poseStack,
            float x,
            float y,
            float z,
            float u,
            float v,
            int packedLight,
            int color) {

        consumer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(0, 1, 0);
    }

    private static void drawQuad(
            VertexConsumer consumer, PoseStack poseStack,
            float minX, float y, float minZ,
            float maxX, float maxZ,
            float u0, float v0, float u1, float v1,
            float spriteV1, int packedLight, int color) {

        drawVertex(consumer, poseStack, minX, y, minZ, u0, v0, packedLight, color);
        drawVertex(consumer, poseStack, minX, y, maxZ, u0, v1, packedLight, color);
        drawVertex(consumer, poseStack, maxX, y, maxZ, u1, v1, packedLight, color);
        drawVertex(consumer, poseStack, maxX, y, minZ, u1, v0, packedLight, color);
    }
}
