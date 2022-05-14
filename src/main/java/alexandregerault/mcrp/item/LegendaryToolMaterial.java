package alexandregerault.mcrp.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class LegendaryToolMaterial implements ToolMaterial {
    @Override
    public int getDurability() {
        return Integer.MAX_VALUE;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1;
    }

    @Override
    public float getAttackDamage() {
        return 5.0f;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 22;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return null;
    }
}
