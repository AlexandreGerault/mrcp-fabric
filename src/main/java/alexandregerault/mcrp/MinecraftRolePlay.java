package alexandregerault.mcrp;

import alexandregerault.mcrp.item.RegisterItems;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinecraftRolePlay implements ModInitializer {
    public static final String MOD_ID = "mcrp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        RegisterItems registerItems = new RegisterItems(new Log4JLogger(LOGGER));
        registerItems.run();
    }
}
