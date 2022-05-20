package alexandregerault.mcrp.item;

import alexandregerault.mcrp.mixin.LivingEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LegendarySwordItem extends SwordItem {
    public static final float HEAL_RATIO = 0.25f;

    public LegendarySwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float amount = ((LivingEntityAccessor) target).getLastDamageTaken();
        attacker.heal(amount * HEAL_RATIO);

        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        return true;
    }
}
