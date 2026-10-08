package net.bananacheese.witchcraft.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
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

    /*
     * Basin geometry, measured from assets/wctda/models/block/alter.json (pixels / 16):
     *   - inner opening of the rim walls:  x and z from 5 to 11 px
     *   - floor of the basin (top of the base slab): y = 4 px
     *   - tall rim walls reach y = 10 px
     * The fluid is inset a hair from the walls (to avoid z-fighting) and fills from just above the
     * floor to one pixel below the rim as the tank fills.
     */
    private static final float BASIN_MIN = 5.15f / 16f;
    private static final float BASIN_MAX = 10.85f / 16f;
    private static final float FLUID_EMPTY_Y = 4.5f / 16f;
    private static final float FLUID_FULL_Y = 9.0f / 16f;

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

        renderFluid(alterBlockEntity, poseStack, buffer);
        renderHeldItem(alterBlockEntity, partialTick, poseStack, buffer);
    }

    private void renderFluid(
            AlterBlockEntity alterBlockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer) {

        FluidStack fluidStack = alterBlockEntity.getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }

        Level level = alterBlockEntity.getLevel();
        if (level == null) {
            return;
        }

        BlockPos pos = alterBlockEntity.getBlockPos();

        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidExtensions.getStillTexture(fluidStack);
        if (stillTexture == null) {
            return;
        }

        FluidState fluidState = fluidStack.getFluid().defaultFluidState();

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        int tintColor = fluidExtensions.getTintColor(fluidState, level, pos);
        if ((tintColor >>> 24) == 0) {
            tintColor |= 0xFF000000; // some fluids return RGB with no alpha, which would render invisible
        }

        // fluid's emit light so lava glows.
        int light = LevelRenderer.getLightColor(level, pos.above());
        int emission = fluidStack.getFluidType().getLightLevel(fluidStack);
        if (emission > 0) {
            light = LightTexture.pack(Math.max(LightTexture.block(light), emission), LightTexture.sky(light));
        }

        float fillRatio = Math.min(1.0f, Math.max(0.0f,
                alterBlockEntity.getFluidAmount() / (float) AlterBlockEntity.MAX_FLUID));
        float y = FLUID_EMPTY_Y + (FLUID_FULL_Y - FLUID_EMPTY_Y) * fillRatio;

        // Crop the sprite to the basin's share of the block so the texture keeps its pixel density.
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float uMin = u0 + (u1 - u0) * BASIN_MIN;
        float uMax = u0 + (u1 - u0) * BASIN_MAX;
        float vMin = v0 + (v1 - v0) * BASIN_MIN;
        float vMax = v0 + (v1 - v0) * BASIN_MAX;

        VertexConsumer consumer = buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluidState));

        // Only the top surface is needed: the rim walls hide the sides of the fluid column.
        drawTopQuad(consumer, poseStack,
                BASIN_MIN, y, BASIN_MIN,
                BASIN_MAX, BASIN_MAX,
                uMin, vMin, uMax, vMax,
                light, tintColor);
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

    /**
     * One upward-facing quad. Vertex order (minX,minZ) -> (minX,maxZ) -> (maxX,maxZ) -> (maxX,minZ)
     * is counter-clockwise seen from above, so it survives back-face culling; same order the Dark Cauldron uses.
     */
    private static void drawTopQuad(
            VertexConsumer consumer, PoseStack poseStack,
            float minX, float y, float minZ,
            float maxX, float maxZ,
            float uMin, float vMin, float uMax, float vMax,
            int packedLight, int color) {

        var matrix = poseStack.last().pose();

        consumer.addVertex(matrix, minX, y, minZ).setColor(color).setUv(uMin, vMin).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, y, maxZ).setColor(color).setUv(uMin, vMax).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, y, maxZ).setColor(color).setUv(uMax, vMax).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, y, minZ).setColor(color).setUv(uMax, vMin).setLight(packedLight).setNormal(0, 1, 0);
    }
}
