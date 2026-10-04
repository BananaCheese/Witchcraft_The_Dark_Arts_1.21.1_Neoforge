package net.bananacheese.witchcraft.block.entity.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class DarkCauldronBlockEntityRenderer implements BlockEntityRenderer<DarkCauldronBlockEntity> {

    // Basin dimensions inside a standard 16x16 block model
    private static final float BOUND_MIN_X = 0.125f; // 2 pixels in
    private static final float BOUND_MAX_X = 0.875f; // 14 pixels in
    private static final float BOUND_MIN_Z = 0.125f;
    private static final float BOUND_MAX_Z = 0.875f;

    private static final float BASIN_BOTTOM = 0.3125f; // 5 pixels high off ground
    private static final float BASIN_TOP = 0.8125f;    // 13 pixels high

    public DarkCauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DarkCauldronBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!entity.isFilled) return;

        Level level = entity.getLevel();
        if (level == null) return;

        FluidStack waterStack = new FluidStack(Fluids.WATER, 1000);
        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(Fluids.WATER);
        ResourceLocation texture = fluidExt.getStillTexture(waterStack);
        if (texture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        BlockPos pos = entity.getBlockPos();
        int tintColor = entity.getFluidColor(); // Dynamic potion/water color

        // Fluid height calculation
        float fillRatio = 1.0f; // Single bucket fill
        float fluidY = BASIN_BOTTOM + ((BASIN_TOP - BASIN_BOTTOM) * fillRatio);

        // Add boiling wave offset if heated
        if (entity.isBoiling()) {
            float gameTime = level.getGameTime() + partialTick;
            fluidY += (float) Math.sin(gameTime * 0.4f) * 0.015f;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());

        // Single top quad facing upward
        drawHorizontalQuad(
                consumer, poseStack,
                BOUND_MIN_X, fluidY, BOUND_MIN_Z,
                BOUND_MAX_X, BOUND_MAX_Z,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                packedLight, tintColor
        );
    }

    private static void drawHorizontalQuad(
            VertexConsumer consumer, PoseStack poseStack,
            float minX, float y, float minZ,
            float maxX, float maxZ,
            float u0, float v0, float u1, float v1,
            int packedLight, int color) {

        var matrix = poseStack.last().pose();

        consumer.addVertex(matrix, minX, y, minZ).setColor(color).setUv(u0, v0).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, y, maxZ).setColor(color).setUv(u0, v1).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, y, maxZ).setColor(color).setUv(u1, v1).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, y, minZ).setColor(color).setUv(u1, v0).setLight(packedLight).setNormal(0, 1, 0);
    }
}