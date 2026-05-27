package cc.ikps.kimi.client.gui.widget;

import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

/**
 * Widget terminal tipo Kali Linux con soporte para:
 * - Movimiento arrastrando
 * - Redimensionamiento
 * - Scroll
 * - Texto animado
 * - Fondo transparente
 */
public class HackingTerminal {
    private int x, y, width, height;
    private String title;
    private String[] lines;
    private List<String> displayLines = new ArrayList<>();
    
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private int scrollOffset = 0;
    private long startTime = System.currentTimeMillis();
    
    private static final int BORDER_COLOR = 0xFF00FF00;
    private static final int TEXT_COLOR = 0xFF00FF00;
    private static final int BG_COLOR = 0x22000000;
    private static final int TITLE_COLOR = 0xFF00FF00;
    private static final float TYPING_SPEED = 50.0f; // ms por carácter
    
    public HackingTerminal(int x, int y, int width, int height, String title, String[] lines) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.lines = lines;
    }
    
    /**
     * Renderiza la terminal
     */
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Dibujar fondo semi-transparente
        drawRect(matrices, x, y, x + width, y + height, BG_COLOR);
        
        // Dibujar borde
        drawBorder(matrices, x, y, x + width, y + height, BORDER_COLOR);
        
        // Dibujar título
        drawString(matrices, title, x + 5, y + 5, TITLE_COLOR);
        
        // Dibujar línea separadora del título
        drawHorizontalLine(matrices, x + 1, y + 18, x + width - 1, BORDER_COLOR);
        
        // Calcular área de contenido
        int contentStartY = y + 25;
        int contentHeight = height - 30;
        int maxLinesVisible = contentHeight / 10;
        
        // Actualizar líneas animadas
        updateAnimatedLines();
        
        // Dibujar líneas de contenido
        int displayIndex = scrollOffset;
        for (int i = 0; i < maxLinesVisible && displayIndex < displayLines.size(); i++) {
            int lineY = contentStartY + (i * 10);
            drawString(matrices, displayLines.get(displayIndex), x + 5, lineY, TEXT_COLOR);
            displayIndex++;
        }
        
        // Dibujar barra de scroll si es necesario
        if (displayLines.size() > maxLinesVisible) {
            drawScrollBar(matrices, maxLinesVisible);
        }
    }
    
    /**
     * Actualiza las líneas con efecto de tipeo
     */
    private void updateAnimatedLines() {
        displayLines.clear();
        long elapsed = System.currentTimeMillis() - startTime;
        
        int totalChars = 0;
        for (String line : lines) {
            long lineStartTime = (long) (totalChars * TYPING_SPEED);
            
            if (elapsed < lineStartTime) {
                // Esta línea aún no debe aparecer
                break;
            }
            
            long lineElapsed = elapsed - lineStartTime;
            int charsToShow = (int) (lineElapsed / TYPING_SPEED);
            
            if (charsToShow >= line.length()) {
                displayLines.add(line);
                totalChars += line.length();
            } else {
                displayLines.add(line.substring(0, Math.min(charsToShow, line.length())) + "_");
                break;
            }
        }
    }
    
    /**
     * Maneja clics del mouse
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Verificar si se hace clic en el título (para arrastrar)
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            isDragging = true;
            dragStartX = (int) mouseX;
            dragStartY = (int) mouseY;
            return true;
        }
        return false;
    }
    
    /**
     * Maneja soltar el mouse
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return false;
    }
    
    /**
     * Actualiza la posición durante arrastre
     */
    public void updatePosition(int dragX, int dragY) {
        if (isDragging) {
            int deltaX = dragX - dragStartX;
            int deltaY = dragY - dragStartY;
            x += deltaX;
            y += deltaY;
            dragStartX = dragX;
            dragStartY = dragY;
        }
    }
    
    /**
     * Maneja scroll
     */
    public void scroll(int amount) {
        int maxLinesVisible = (height - 30) / 10;
        scrollOffset -= amount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, displayLines.size() - maxLinesVisible)));
    }
    
    /**
     * Redimensiona la terminal
     */
    public void resize(int newWidth, int newHeight) {
        this.width = Math.max(150, newWidth);
        this.height = Math.max(100, newHeight);
    }
    
    /**
     * Verifica si el mouse está sobre la terminal
     */
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    /**
     * Verifica si está siendo arrastrada
     */
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Dibuja un rectángulo relleno
     */
    private void drawRect(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        var matrix = matrices.peek().getPositionMatrix();
        
        // Aquí iría el código para dibujar con VertexConsumer
        // Por ahora, placeholder para compilación
    }
    
    /**
     * Dibuja un borde
     */
    private void drawBorder(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        // Línea superior
        drawHorizontalLine(matrices, x1, y1, x2, color);
        // Línea inferior
        drawHorizontalLine(matrices, x1, y2, x2, color);
        // Línea izquierda
        drawVerticalLine(matrices, x1, y1, y2, color);
        // Línea derecha
        drawVerticalLine(matrices, x2, y1, y2, color);
    }
    
    /**
     * Dibuja una línea horizontal
     */
    private void drawHorizontalLine(MatrixStack matrices, int x1, int y, int x2, int color) {
        // Placeholder para compilación
    }
    
    /**
     * Dibuja una línea vertical
     */
    private void drawVerticalLine(MatrixStack matrices, int x, int y1, int y2, int color) {
        // Placeholder para compilación
    }
    
    /**
     * Dibuja texto
     */
    private void drawString(MatrixStack matrices, String text, int x, int y, int color) {
        // Placeholder para compilación
        // En implementación real: this.client.textRenderer.draw(matrices, text, x, y, color);
    }
    
    /**
     * Dibuja barra de scroll
     */
    private void drawScrollBar(MatrixStack matrices, int maxLinesVisible) {
        int scrollBarX = x + width - 5;
        int scrollBarHeight = height - 30;
        int scrollBarY = y + 25;
        
        if (displayLines.size() > 0) {
            int scrollHeight = (int) ((float) maxLinesVisible / displayLines.size() * scrollBarHeight);
            int scrollPos = (int) ((float) scrollOffset / displayLines.size() * scrollBarHeight);
            
            // Dibujar background de scroll
            drawRect(matrices, scrollBarX, scrollBarY, scrollBarX + 3, scrollBarY + scrollBarHeight, 0xFF333333);
            // Dibujar posición de scroll
            drawRect(matrices, scrollBarX, scrollBarY + scrollPos, scrollBarX + 3, scrollBarY + scrollPos + scrollHeight, BORDER_COLOR);
        }
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTitle() { return title; }
}
