package cc.ikps.kimi.client.gui.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Botón personalizado con estilo terminal
 */
public class TextButton extends ButtonWidget {
    private static final int BORDER_COLOR = 0xFF00FF00;
    private static final int TEXT_COLOR = 0xFF00FF00;
    private static final int BG_COLOR = 0x00000000;
    private static final int HOVER_COLOR = 0xFF00FF00;

    public TextButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Override
    public void renderWidget(net.minecraft.client.util.math.MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Renderizar botón personalizado
        if (this.isHovered()) {
            // Fondo cuando está hover
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x4400FF00);
        }

        // Dibujar borde
        drawBorder(matrices, this.x, this.y, this.x + this.width, this.y + this.height, BORDER_COLOR);

        // Dibujar texto
        drawCenteredString(matrices, this.client.textRenderer, this.getMessage(), 
            this.x + this.width / 2, this.y + (this.height - 8) / 2, TEXT_COLOR);
    }

    private static void drawBorder(net.minecraft.client.util.math.MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        fill(matrices, x1, y1, x2, y1 + 1, color);
        fill(matrices, x1, y2 - 1, x2, y2, color);
        fill(matrices, x1, y1, x1 + 1, y2, color);
        fill(matrices, x2 - 1, y1, x2, y2, color);
    }
}
