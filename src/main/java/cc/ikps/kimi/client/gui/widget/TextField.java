package cc.ikps.kimi.client.gui.widget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/**
 * Campo de texto personalizado con estilo terminal
 */
public class TextField extends TextFieldWidget {
    private static final int CURSOR_COLOR = 0xFF00FF00;
    private static final int TEXT_COLOR = 0xFF00FF00;
    private static final int BORDER_COLOR = 0xFF00FF00;

    public TextField(int x, int y, int width, int height, String initialValue) {
        super(null, x, y, width, height, Text.literal(initialValue));
        this.setText(initialValue);
    }

    public void setValue(String value) {
        this.setText(value);
    }

    public String getValue() {
        return this.getText();
    }

    public void setSecret(boolean secret) {
        // TODO: Implementar campo secreto
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Dibujar fondo
        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x00000000);

        // Dibujar borde
        drawBorder(matrices, this.x, this.y, this.x + this.width, this.y + this.height, BORDER_COLOR);

        // Dibujar texto
        String text = this.getText();
        if (text.isEmpty()) {
            // Mostrar placeholder en gris
            // this.client.textRenderer.draw(matrices, placeholder, this.x + 4, this.y + 6, 0xFF555555);
        } else {
            // this.client.textRenderer.draw(matrices, text, this.x + 4, this.y + 6, TEXT_COLOR);
        }

        // Cursor parpadeante
        if (this.isFocused()) {
            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) {
                int cursorX = this.x + 4 + (text.length() * 6);
                fill(matrices, cursorX, this.y + 2, cursorX + 1, this.y + this.height - 2, CURSOR_COLOR);
            }
        }
    }

    private static void drawBorder(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        fill(matrices, x1, y1, x2, y1 + 1, color);
        fill(matrices, x1, y2 - 1, x2, y2, color);
        fill(matrices, x1, y1, x1 + 1, y2, color);
        fill(matrices, x2 - 1, y1, x2, y2, color);
    }

    private static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        int temp;
        if (x1 < x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 < y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        var matrix = matrices.peek().getPositionMatrix();
        var vertexConsumer = net.minecraft.client.render.VertexConsumerProvider.immediate(
            new net.minecraft.client.render.BufferBuilder(256)
        ).getBuffer(net.minecraft.client.render.RenderLayer.getGuiOverlay());

        vertexConsumer.vertex(matrix, (float) x1, (float) y2, 0.0F).color(r, g, b, a).next();
        vertexConsumer.vertex(matrix, (float) x2, (float) y2, 0.0F).color(r, g, b, a).next();
        vertexConsumer.vertex(matrix, (float) x2, (float) y1, 0.0F).color(r, g, b, a).next();
        vertexConsumer.vertex(matrix, (float) x1, (float) y1, 0.0F).color(r, g, b, a).next();
    }
}
