package alexandregerault.mcrp.lock;

import alexandregerault.mcrp.MinecraftRolePlay;
import alexandregerault.mcrp.lock.mixin.MixinDoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.tag.TagKey;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class KeyItem extends Item {
    public KeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockState hitBlock = world.getBlockState(context.getBlockPos());
        ItemStack key = context.getStack();
        NbtCompound nbt = key.getNbt();
        PlayerEntity player = context.getPlayer();

        if (world.isClient || !isLockable(hitBlock) || player == null) {
            return super.useOnBlock(context);
        }

        if (nbt != null && hasAlreadyLockedDoor(nbt)) {
            if (NbtHelper.toBlockPos(nbt.getCompound("DoorPosition")).equals(context.getBlockPos())) {
                player.sendMessage(new TranslatableText("item.mcrp.key_item.unlocking"), true);
                world.setBlockState(context.getBlockPos(), hitBlock
                        .with(Properties.LOCKED, false)
                        .with(Properties.LOCKED, false)
                );
                forgetDoor(key);
            } else {
                player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_door"), true);
            }
        } else if (nbt == null) {
            player.sendMessage(new TranslatableText("item.mcrp.key_item.locking"), true);
            world.setBlockState(context.getBlockPos(), hitBlock.with(Properties.LOCKED, true));
            this.writeNbt(world.getRegistryKey(), context.getBlockPos(), key.getOrCreateNbt());
        }

        return super.useOnBlock(context);
    }

    private void writeNbt(RegistryKey<World> worldRegistryKey, BlockPos pos, NbtCompound nbtCompound) {
        nbtCompound.put("DoorPosition", NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldRegistryKey)
                .resultOrPartial(MinecraftRolePlay.LOGGER::error)
                .ifPresent(nbtElement -> nbtCompound.put("DoorDimension", nbtElement));
    }

    private void forgetDoor(ItemStack key) {
        key.setNbt(null);
    }

    private boolean isLockable(BlockState target) {
        return target.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(MinecraftRolePlay.MOD_ID, "lockable")));
    }

    private boolean hasAlreadyLockedDoor(NbtCompound nbt) {
        return nbt.contains("DoorPosition") && nbt.contains("DoorDimension");
    }
}
