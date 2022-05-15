package alexandregerault.mcrp.item;

import alexandregerault.mcrp.ILogger;
import alexandregerault.mcrp.MinecraftRolePlay;
import alexandregerault.mcrp.lock.KeyItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class RegisterItems {
    public static final Item CORNUCOPIA_ITEM = new CornucopiaItem(new FabricItemSettings().group(ItemGroup.MISC).maxDamage(1));
    public static final Item LEGENDARY_SWORD_ITEM = new LegendarySwordItem(
            new LegendaryToolMaterial(),
            3,
            -2.0f,
            new FabricItemSettings().group(ItemGroup.COMBAT).fireproof().rarity(Rarity.EPIC)
    );

    public static final Item KEY_ITEM = new KeyItem(new FabricItemSettings().group(ItemGroup.MISC));

    private final ILogger logger;

    public RegisterItems(ILogger logger) {
        this.logger = logger;
    }

    public void run() {
        logger.info("Registering items");
        Registry.register(Registry.ITEM, new Identifier(MinecraftRolePlay.MOD_ID, "cornucopia_item"), CORNUCOPIA_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MinecraftRolePlay.MOD_ID, "legendary_sword_item"), LEGENDARY_SWORD_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MinecraftRolePlay.MOD_ID, "key_item"), KEY_ITEM);
        logger.info("Items registered successfully");
    }
}
