package cc.ikps.kimi;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KimiClient implements ModInitializer {
    public static final String MOD_ID = "kimiclient";
    public static final String MOD_NAME = "Kimi-Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);
    }
}
