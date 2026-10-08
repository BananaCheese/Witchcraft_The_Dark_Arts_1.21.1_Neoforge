package net.bananacheese.witchcraft.block.entity.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class DarkCauldronBlockEntityRenderer implements BlockEntityRenderer<DarkCauldronBlockEntity> {

    // Basin opening: the old 1x1 values (0.125..0.875) doubled, since the model is stretched 2x in X/Z.
    private static final float BOUND_MIN = 0.25f;
    private static final float BOUND_MAX = 1.75f;
    private static final float CENTER = 1.0f;

    private static final float BASIN_BOTTOM = 0.3125f; // 5 pixels high off ground (height is not stretched)
    private static final float BASIN_TOP = 0.8125f;    // 13 pixels high

    // Floating ingredients
    private static final float ORBIT_RADIUS = 0.6f;
    private static final float ORBIT_SPEED = 0.02f;          // radians per tick
    private static final float ORBIT_SPEED_BOILING = 0.035f;
    private static final float ITEM_HOVER = 0f;           // above the liquid surface
    private static final float ITEM_SCALE = 0.45f;

    public DarkCauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    // The block entity lives in one block but draws across 2x2: without this it is culled when its corner leaves the screen.
    @Override
    public AABB getRenderBoundingBox(DarkCauldronBlockEntity entity) {
        BlockPos pos = entity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
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

        // The block entity's own position is inside a non-occluding model; light the liquid from the air above it.
        int light = LevelRenderer.getLightColor(level, pos.above());

        // Fluid height calculation
        float fillRatio = 1.0f; // Single bucket fill
        float fluidY = BASIN_BOTTOM + ((BASIN_TOP - BASIN_BOTTOM) * fillRatio);

        // Add boiling wave offset if heated
        float time = level.getGameTime() + partialTick;
        if (entity.isBoiling()) {
            fluidY += (float) Math.sin(time * 0.4f) * 0.015f;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());

        // The surface is 1.5 blocks across. Draw it as a 2x2 grid of quads, each showing one full copy of the
        // texture, so the water keeps a sensible pixel density instead of one stretched tile.
        float half = (BOUND_MAX - BOUND_MIN) / 2f;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                float x0 = BOUND_MIN + i * half;
                float z0 = BOUND_MIN + j * half;
                drawHorizontalQuad(
                        consumer, poseStack,
                        x0, fluidY, z0,
                        x0 + half, z0 + half,
                        sprite.getU0(), sprite.getV0(),
                        sprite.getU1(), sprite.getV1(),
                        light, tintColor
                );
            }
        }

        renderFloatingIngredients(entity, level, time, fluidY, poseStack, buffer, light);
    }

    /** Ingredients drift in a slow ring above the surface, bobbing and spinning; faster while boiling. */
    private static void renderFloatingIngredients(DarkCauldronBlockEntity entity, Level level, float time, float fluidY,
                                                  PoseStack poseStack, MultiBufferSource buffer, int light) {
        List<ItemStack> ingredients = entity.getIngredients();
        if (ingredients.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float speed = entity.isBoiling() ? ORBIT_SPEED_BOILING : ORBIT_SPEED;
        int count = ingredients.size();

        for (int i = 0; i < count; i++) {
            float angle = time * speed + i * (float) (2.0 * Math.PI / count);
            float x = CENTER + (float) Math.cos(angle) * ORBIT_RADIUS;
            float z = CENTER + (float) Math.sin(angle) * ORBIT_RADIUS;
            float y = fluidY + ITEM_HOVER + (float) Math.sin(time * 0.08f + i * 1.7f) * 0.04f;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f + i * 60.0f));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            itemRenderer.renderStatic(
                    ingredients.get(i),
                    ItemDisplayContext.GROUND,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    level,
                    i);

            poseStack.popPose();
        }
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