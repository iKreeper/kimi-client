package cc.ikps.kimi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class KimiClientClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KimiClient.LOGGER.info("Initializing {} Client", KimiClient.MOD_NAME);
    }
}
