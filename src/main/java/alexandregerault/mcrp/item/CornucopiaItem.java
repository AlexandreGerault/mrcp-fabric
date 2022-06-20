package alexandregerault.mcrp.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CornucopiaItem extends Item {
    public CornucopiaItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack cornucopia = hand.equals(Hand.MAIN_HAND) ? user.getMainHandStack() : user.getOffHandStack();
        ItemStack target = hand.equals(Hand.MAIN_HAND) ? user.getOffHandStack().copy() : user.getMainHandStack().copy();
        Text name = target.getName();

        if (target.isEmpty()) {
            user.sendMessage(Text.of("You have no item to duplicate"), true);
            return super.use(world, user, hand);
        }

        target.setCount(1);
        user.getInventory().insertStack(target);

        cornucopia.damage(1, user, (u) -> {
            u.sendMessage(Text.translatable("item.mcrp.cornucopia_item.used", cornucopia.getName(), name), true);
            u.sendMessage(Text.translatable("item.mcrp.cornucopia_item.used", cornucopia.getName(), name), false);
        });

        return super.use(world, user, hand);
    }
}
