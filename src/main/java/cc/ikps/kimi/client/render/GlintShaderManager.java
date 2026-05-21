package cc.ikps.kimi.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderiza objetivos ESP con efecto Glint/Glow usando shaders
 */
public class GlintShaderManager {
    private static final float GLINT_SPEED = 100.0f;
    private static final float GLINT_INTENSITY = 0.8f;
    private static final float GLOW_WIDTH = 0.1f;

    private final Map<String, ESPRenderer.ESPTarget> glintTargets = new HashMap<>();
    private float glintOffset = 0.0f;

    public void addGlintTarget(ESPRenderer.ESPTarget target) {
        glintTargets.put(target.uniqueId, target);
    }

    public void removeGlintTarget(String uniqueId) {
        glintTargets.remove(uniqueId);
    }

    public void clearAllTargets() {
        glintTargets.clear();
    }

    /**
     * Renderiza todos los objetivos con efecto glint
     * Debe ser llamado desde un WorldRenderer mixin
     */
    public void render() {
        if (glintTargets.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Actualizar offset del glint para animación
        glintOffset += GLINT_SPEED * 0.016f; // ~60fps
        if (glintOffset > 360) glintOffset -= 360;

        // Renderizar entidades
        for (ESPRenderer.ESPTarget target : glintTargets.values()) {
            if (target.entity != null && target.entity.isAlive()) {
                renderEntityGlint(client, target);
            } else if (target.blockPos != null) {
                renderBlockGlint(client, target);
            }
        }
    }

    /**
     * Renderiza glint alrededor de una entidad
     */
    private void renderEntityGlint(MinecraftClient client, ESPRenderer.ESPTarget target) {
        Entity entity = target.entity;
        Vec3d pos = entity.getPos();
        Box box = entity.getBoundingBox().offset(
            entity.getX() - pos.x,
            entity.getY() - pos.y,
            entity.getZ() - pos.z
        );

        renderGlintBox(client, box, target.colorHex);

        // Renderizar líneas de identificación (solo para players)
        if (target.type == ESPRenderer.ESPType.PLAYER) {
            renderPlayerIdentifier(client, entity, target);
        }
    }

    /**
     * Renderiza glint alrededor de un bloque
     */
    private void renderBlockGlint(MinecraftClient client, ESPRenderer.ESPTarget target) {
        BlockPos pos = target.blockPos;
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
        renderGlintBox(client, box, target.colorHex);
    }

    /**
     * Dibuja un box con efecto glint/glow
     */
    private void renderGlintBox(MinecraftClient client, Box box, int colorHex) {
        MatrixStack matrices = new MatrixStack();
        matrices.push();

        // Extraer componentes RGB
        int r = (colorHex >> 16) & 0xFF;
        int g = (colorHex >> 8) & 0xFF;
        int b = colorHex & 0xFF;
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        // Configurar estado de renderizado
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();

        VertexConsumer vertexConsumer = client.getBufferBuilders().getEntityVertexConsumers()
            .getBuffer(net.minecraft.client.render.RenderLayer.getLines());

        // Dibujar líneas del box con efecto pulsante
        float pulse = (float) (Math.sin(Math.toRadians(glintOffset)) * 0.5f + 1.5f);
        drawBoxLines(matrices, vertexConsumer, box, rf, gf, bf, pulse * GLINT_INTENSITY);

        matrices.pop();
        RenderSystem.disableBlend();
    }

    /**
     * Dibuja las líneas del box
     */
    private void drawBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer,
                              Box box, float r, float g, float b, float alpha) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Líneas inferiores
        drawLine(matrices, vertexConsumer, minX, minY, minZ, maxX, minY, minZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, minX, minY, maxZ, minX, minY, minZ, r, g, b, alpha);

        // Líneas superiores
        drawLine(matrices, vertexConsumer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, alpha);

        // Líneas verticales
        drawLine(matrices, vertexConsumer, minX, minY, minZ, minX, maxY, minZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, alpha);
        drawLine(matrices, vertexConsumer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
    }

    /**
     * Dibuja una línea individual
     */
    private void drawLine(MatrixStack matrices, VertexConsumer vertexConsumer,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float alpha) {
        var matrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).next();
        vertexConsumer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).next();
    }

    /**
     * Renderiza identificador del jugador (nombre/UUID)
     */
    private void renderPlayerIdentifier(MinecraftClient client, Entity entity, ESPRenderer.ESPTarget target) {
        // TODO: Implementar renderizado de nombre/UUID sobre el jugador
        // Esto se hará con el sistema de text renderer de Minecraft
    }
}
