package alexandregerault.mcrp.lock;

import alexandregerault.mcrp.MinecraftRolePlay;
import alexandregerault.mcrp.lock.mixin.MixinDoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
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

        BlockPos otherHalfPos = world.getBlockState(context.getBlockPos()).get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                ? context.getBlockPos().offset(Direction.Axis.Y, -1)
                : context.getBlockPos().offset(Direction.Axis.Y, 1);
        BlockState otherHalf = world.getBlockState(otherHalfPos);

        if (nbt != null && hasAlreadyLockedDoor(nbt)) {
            if (isRecordedDoor(context, nbt, otherHalfPos)) {
                player.sendMessage(new TranslatableText("item.mcrp.key_item.unlocking"), true);
                unlockHalfDoor(context.getBlockPos(), world, hitBlock);
                unlockHalfDoor(otherHalfPos, world, otherHalf);
                forgetDoor(key);
            } else {
                player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_door"), true);
            }
        } else {
            player.sendMessage(new TranslatableText("item.mcrp.key_item.locking"), true);
            lockHalfDoor(context.getBlockPos(), world, hitBlock);
            lockHalfDoor(otherHalfPos, world, otherHalf);
            this.writeNbt(world.getRegistryKey(), context.getBlockPos(), key.getOrCreateNbt());
        }

        return super.useOnBlock(context);
    }

    private void unlockHalfDoor(BlockPos pos, World world, BlockState hitBlock) {
        world.setBlockState(
                pos,
                hitBlock.with(Properties.LOCKED, false).with(net.minecraft.state.property.Properties.LOCKED, false)
        );
    }

    private void lockHalfDoor(BlockPos pos, World world, BlockState hitBlock) {
        world.setBlockState(
                pos,
                hitBlock.with(Properties.LOCKED, true)
        );
    }

    private boolean isRecordedDoor(ItemUsageContext context, NbtCompound nbt, BlockPos otherHalfPos) {
        return NbtHelper.toBlockPos(nbt.getCompound("DoorPosition")).equals(context.getBlockPos()) || NbtHelper.toBlockPos(nbt.getCompound("DoorPosition")).equals(otherHalfPos);
    }

    private void writeNbt(RegistryKey<World> worldRegistryKey, BlockPos pos, NbtCompound nbtCompound) {
        nbtCompound.put("DoorPosition", NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldRegistryKey)
                .resultOrPartial(MinecraftRolePlay.LOGGER::error)
                .ifPresent(nbtElement -> nbtCompound.put("DoorDimension", nbtElement));
    }

    private void forgetDoor(ItemStack key) {
        if (key.getNbt() != null) {
            key.getNbt().remove("DoorPosition");
            key.getNbt().remove("DoorDimension");
        }
    }

    private boolean isLockable(BlockState target) {
        return target.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(MinecraftRolePlay.MOD_ID, "lockable")));
    }

    private boolean hasAlreadyLockedDoor(NbtCompound nbt) {
        return nbt.contains("DoorPosition") && nbt.contains("DoorDimension");
    }
}
