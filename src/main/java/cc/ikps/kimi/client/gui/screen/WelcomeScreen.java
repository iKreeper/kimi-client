package cc.ikps.kimi.client.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de bienvenida con 6 terminales simulando "hacking"
 * Aparecen de forma secuencial y el menú principal aparece después de 5 segundos
 */
public class WelcomeScreen extends Screen {
    private static final long START_TIME = System.currentTimeMillis();
    private static final long TERMINAL_DELAY = 500; // Milisegundos entre cada terminal
    private static final long MENU_APPEARANCE_TIME = 5000; // Tiempo para que aparezca el menú

    private final List<HackingTerminal> terminals = new ArrayList<>();
    private HackingTerminal menuTerminal;
    private boolean menuAppeared = false;

    public WelcomeScreen() {
        super(Text.literal("Kimi-Client Welcome"));
        initializeTerminals();
    }

    private void initializeTerminals() {
        // Terminal 1: Login Session (aparece inmediatamente)
        terminals.add(new HackingTerminal(
            50, 50, 350, 150,
            "LOGIN_SESSION",
            new String[]{
                "kimi@client:~$ sudo su",
                "[sudo] password for kimi: ••••••••",
                "root@client:~# ",
                "root@client:~# _"
            }
        ));

        // Terminal 2: Network Info (aparece después de 500ms)
        terminals.add(new HackingTerminal(
            420, 50, 350, 150,
            "NETWORK_INFO",
            new String[]{
                "root@client:~# ifconfig",
                "eth0: flags=UP,BROADCAST,RUNNING",
                "  inet 192.168.1.100",
                "  netmask 255.255.255.0",
                "root@client:~# _"
            }
        ));

        // Terminal 3: Module Scan (aparece después de 1000ms)
        terminals.add(new HackingTerminal(
            50, 220, 350, 150,
            "MODULE_SCAN",
            new String[]{
                "root@client:~# scan_modules.sh",
                "[*] Scanning modules...",
                "[+] Fly Module........OK",
                "[+] XRay Module.......OK",
                "[+] ESP Module........OK",
                "root@client:~# _"
            }
        ));

        // Terminal 4: Filesystem (aparece después de 1500ms)
        terminals.add(new HackingTerminal(
            420, 220, 350, 150,
            "FILESYSTEM",
            new String[]{
                "root@client:~# ls -la ~/.kimi/",
                "drwxr-xr-x config",
                "drwxr-xr-x macros",
                "drwxr-xr-x preferences",
                "-rw-r--r-- auth.enc",
                "root@client:~# _"
            }
        ));

        // Terminal 5: System Status (aparece después de 2000ms)
        terminals.add(new HackingTerminal(
            50, 390, 350, 150,
            "STATUS",
            new String[]{
                "root@client:~# system_status",
                "System: Kimi-Client v1.0.0",
                "Memory: 2048MB | CPU: 95%",
                "Modules: 5/5 Loaded",
                "Status: [████████████] READY",
                "root@client:~# _"
            }
        ));

        // Terminal 6: Menu (aparece después de 5 segundos con botones)
        menuTerminal = new HackingTerminal(
            420, 390, 350, 150,
            "MAIN_MENU",
            new String[]{
                "╔════════════════════════╗",
                "║  KIMI-CLIENT MAIN MENU ║",
                "║  [1] Join World        ║",
                "║  [2] Modules           ║",
                "║  [3] Preferences       ║",
                "║  [4] Macros            ║",
                "║  [5] Settings          ║",
                "║  [6] Exit              ║",
                "╚════════════════════════╝",
                "root@client:~# _"
            }
        );
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long elapsed = System.currentTimeMillis() - START_TIME;

        // Renderizar fondo del juego
        this.renderDirtBackground(matrices);

        // Renderizar terminales según el tiempo transcurrido
        for (int i = 0; i < terminals.size(); i++) {
            long showAt = i * TERMINAL_DELAY;
            if (elapsed >= showAt) {
                terminals.get(i).render(matrices, mouseX, mouseY, delta);
            }
        }

        // Mostrar menú principal después de 5 segundos
        if (elapsed >= MENU_APPEARANCE_TIME && !menuAppeared) {
            menuAppeared = true;
        }

        if (menuAppeared && menuTerminal != null) {
            menuTerminal.render(matrices, mouseX, mouseY, delta);
            // Aquí se pueden agregar botones interactivos del menú
        }

        // Mostrar porcentaje de carga
        if (elapsed < MENU_APPEARANCE_TIME) {
            int progress = (int) ((elapsed / (float) MENU_APPEARANCE_TIME) * 100);
            drawCenteredString(matrices, this.textRenderer,
                String.format("[%s] Loading... %d%%", getProgressBar(progress), progress),
                this.width / 2, this.height - 30, 0xFF00FF00);
        }
    }

    private String getProgressBar(int progress) {
        int filled = progress / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Permitir scroll en las terminales
        for (HackingTerminal terminal : terminals) {
            if (terminal.isHovered(mouseX, mouseY)) {
                terminal.scroll((int) amount);
            }
        }
        if (menuTerminal != null && menuTerminal.isHovered(mouseX, mouseY)) {
            menuTerminal.scroll((int) amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Permitir mover terminales
        for (HackingTerminal terminal : terminals) {
            if (terminal.isDragging()) {
                terminal.updatePosition((int) dragX, (int) dragY);
            }
        }
        if (menuTerminal != null && menuTerminal.isDragging()) {
            menuTerminal.updatePosition((int) dragX, (int) dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Permitir interactuar con terminales
        for (HackingTerminal terminal : terminals) {
            if (terminal.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (menuTerminal != null && menuTerminal.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // No se puede cerrar con ESC durante la bienvenida
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
