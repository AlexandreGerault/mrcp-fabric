package alexandregerault.mcrp.entity.damage;

import net.minecraft.entity.damage.DamageSource;

public class MinecraftRolePlayDamageSource extends DamageSource {
    public static final DamageSource ABUSED_GOD_STICK = (new MinecraftRolePlayDamageSource("abused_god_stick")).setBypassesArmor();
    public MinecraftRolePlayDamageSource(String name) {
        super(name);
    }
}
