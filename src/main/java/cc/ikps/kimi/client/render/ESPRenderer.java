package cc.ikps.kimi.client.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;

/**
 * Renderiza objetos y entidades con efecto glint/glow basado en shader
 * Soporta: Players, Mobs, Chests, Bloques personalizados
 */
public class ESPRenderer {
    private static final Map<String, ESPTarget> trackedTargets = new HashMap<>();
    private static final GlintShaderManager shaderManager = new GlintShaderManager();

    public static class ESPTarget {
        public Entity entity;
        public BlockPos blockPos;
        public ESPType type;
        public int colorHex;
        public String uniqueId;

        public ESPTarget(Entity entity, ESPType type, int colorHex) {
            this.entity = entity;
            this.type = type;
            this.colorHex = colorHex;
            this.uniqueId = generateId(entity);
        }

        public ESPTarget(BlockPos pos, ESPType type, int colorHex, String blockId) {
            this.blockPos = pos;
            this.type = type;
            this.colorHex = colorHex;
            this.uniqueId = blockId + "_" + pos.toShortString();
        }

        private static String generateId(Entity entity) {
            return entity.getClass().getSimpleName() + "_" + entity.getId();
        }
    }

    public enum ESPType {
        PLAYER("Player", 0xFF00FF00),        // Verde
        HOSTILE_MOB("HostileMob", 0xFFFF0000), // Rojo
        PASSIVE_MOB("PassiveMob", 0xFFFFFF00), // Amarillo
        CHEST("Chest", 0xFFFF8C00),           // Naranja
        BLOCK("Block", 0xFF0099FF);           // Cian

        public final String name;
        public final int defaultColor;

        ESPType(String name, int defaultColor) {
            this.name = name;
            this.defaultColor = defaultColor;
        }
    }

    /**
     * Registra una entidad para renderizado ESP
     */
    public static void addPlayerESP(PlayerEntity player) {
        if (player != null) {
            int color = getColorByPlayerId(player.getUuid().hashCode());
            ESPTarget target = new ESPTarget(player, ESPType.PLAYER, color);
            trackedTargets.put(target.uniqueId, target);
            shaderManager.addGlintTarget(target);
        }
    }

    /**
     * Registra un mob para renderizado ESP
     */
    public static void addMobESP(LivingEntity entity) {
        if (entity != null) {
            ESPType type = entity instanceof HostileEntity ? ESPType.HOSTILE_MOB : ESPType.PASSIVE_MOB;
            int color = getColorByEntityType(entity.getClass().getSimpleName());
            ESPTarget target = new ESPTarget(entity, type, color);
            trackedTargets.put(target.uniqueId, target);
            shaderManager.addGlintTarget(target);
        }
    }

    /**
     * Registra un cofre para renderizado ESP
     */
    public static void addChestESP(BlockPos pos, String chestType) {
        int color = getColorByBlockType(chestType);
        ESPTarget target = new ESPTarget(pos, ESPType.CHEST, color, chestType);
        trackedTargets.put(target.uniqueId, target);
        shaderManager.addGlintTarget(target);
    }

    /**
     * Registra un bloque personalizado para renderizado ESP
     */
    public static void addBlockESP(BlockPos pos, String blockType) {
        int color = getColorByBlockType(blockType);
        ESPTarget target = new ESPTarget(pos, ESPType.BLOCK, color, blockType);
        trackedTargets.put(target.uniqueId, target);
        shaderManager.addGlintTarget(target);
    }

    /**
     * Elimina una entidad del renderizado ESP
     */
    public static void removeESP(String uniqueId) {
        trackedTargets.remove(uniqueId);
        shaderManager.removeGlintTarget(uniqueId);
    }

    /**
     * Limpia todos los objetivos ESP
     */
    public static void clearAll() {
        trackedTargets.clear();
        shaderManager.clearAllTargets();
    }

    /**
     * Obtiene color por ID de jugador (consistente para el mismo jugador)
     */
    private static int getColorByPlayerId(int hashCode) {
        String[] colors = {
            "FF00FF00", // Verde
            "FF0099FF", // Cian
            "FFFF00FF", // Magenta
            "FF00FFFF", // Agua
            "FF99FF00"  // Lima
        };
        return (int) Long.parseLong(colors[Math.abs(hashCode) % colors.length], 16);
    }

    /**
     * Obtiene color por tipo de entidad
     */
    private static int getColorByEntityType(String entityType) {
        return entityType.hashCode() % 2 == 0 ? 0xFFFF0000 : 0xFFFF8800; // Rojo u Naranja
    }

    /**
     * Obtiene color por tipo de bloque (basado en su ID)
     */
    private static int getColorByBlockType(String blockType) {
        int hash = blockType.hashCode();
        // Genera color consistente basado en el tipo de bloque
        int r = Math.abs(hash) % 256;
        int g = Math.abs(hash >> 8) % 256;
        int b = Math.abs(hash >> 16) % 256;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Obtiene todos los objetivos rastreados
     */
    public static Map<String, ESPTarget> getTrackedTargets() {
        return new HashMap<>(trackedTargets);
    }

    /**
     * Renderiza los objetivos ESP (llamado cada frame)
     */
    public static void render() {
        shaderManager.render();
    }
}
