package alexandregerault.mcrp.lock;

import alexandregerault.mcrp.MinecraftRolePlay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

public class KeyItem extends Item {
    public KeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockEntity blockEntity = world.getBlockEntity(context.getBlockPos());
        PlayerEntity player = context.getPlayer();

        LockableBlockEntity upperBlockEntity = (LockableBlockEntity) (world.getBlockState(context.getBlockPos()).get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                ? world.getBlockEntity(context.getBlockPos())
                : world.getBlockEntity(context.getBlockPos().offset(Direction.Axis.Y, 1)));
        LockableBlockEntity lowerBlockEntity = (LockableBlockEntity) (world.getBlockState(context.getBlockPos()).get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER)
                ? world.getBlockEntity(context.getBlockPos())
                : world.getBlockEntity(context.getBlockPos().offset(Direction.Axis.Y, -1)));

        if (world.isClient || isNotLockable(blockEntity) || player == null || upperBlockEntity == null || lowerBlockEntity == null) {
            MinecraftRolePlay.LOGGER.info("Something is null: {}, {}, {}, {}, {}", world.isClient, isNotLockable(blockEntity), player == null, upperBlockEntity == null, lowerBlockEntity == null);
            return super.useOnBlock(context);
        }

        ItemStack key = context.getStack();
        NbtCompound nbt = key.getNbt();

        if (nbt == null) {
            return lockDoor(context, key, player, upperBlockEntity, lowerBlockEntity);
        }

        if (!hasAlreadyLockedDoor(nbt)) {
            return super.useOnBlock(context);
        }

        if (isRecordedBlockEntity(nbt, upperBlockEntity) && isRecordedBlockEntity(nbt, lowerBlockEntity)) {
            MinecraftRolePlay.LOGGER.info("This is the recorded block entity");
            return upperBlockEntity.isLocked()
                    ? unlockDoor(context, key, player, upperBlockEntity, lowerBlockEntity)
                    : lockDoor(context, key, player, upperBlockEntity, lowerBlockEntity);
        }

        player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_door"), true);
        return super.useOnBlock(context);
    }

    private ActionResult lockDoor(ItemUsageContext context, ItemStack key, PlayerEntity player, LockableBlockEntity... blockEntities) {
        player.sendMessage(new TranslatableText("item.mcrp.key_item.locking"), true);
        for (LockableBlockEntity blockEntity: blockEntities) {
            MinecraftRolePlay.LOGGER.info("Lock entity: {}", blockEntity.uuid());
            lock(blockEntity);
        }
        this.writeNbt(key.getOrCreateNbt(), blockEntities);
        return super.useOnBlock(context);
    }

    private ActionResult unlockDoor(ItemUsageContext context, ItemStack key, PlayerEntity player, LockableBlockEntity... blockEntities) {
        player.sendMessage(new TranslatableText("item.mcrp.key_item.unlocking"), true);
        for (LockableBlockEntity blockEntity: blockEntities) {
            MinecraftRolePlay.LOGGER.info("Unlock entity: {}", blockEntity.uuid());
            unlock(blockEntity);
        }
        this.writeNbt(key.getOrCreateNbt(), blockEntities);
        return super.useOnBlock(context);
    }

    private void unlock(LockableBlockEntity blockEntity) {
        blockEntity.unlock();
    }

    private void lock(LockableBlockEntity blockEntity) {
        blockEntity.lock();
    }

    private boolean isRecordedBlockEntity(NbtCompound nbt, LockableBlockEntity blockEntity) {
        NbtList uuids = nbt.getList("DoorUuids", NbtList.INT_ARRAY_TYPE);

        for (NbtElement nbtElement : uuids) {
            UUID uuid = NbtHelper.toUuid(nbtElement);
            if (uuid.equals(blockEntity.uuid())) {
                return true;
            }
        }

        return false;
    }

    private void writeNbt(NbtCompound nbtCompound, LockableBlockEntity... targets) {
        NbtList uuids = new NbtList();
        for(LockableBlockEntity target: targets) {
            uuids.add(NbtHelper.fromUuid(target.uuid()));
        }
        nbtCompound.put("DoorUuids", uuids);
    }

    private boolean isNotLockable(BlockEntity target) {
        return !(target instanceof LockableBlockEntity);
    }

    private boolean hasAlreadyLockedDoor(NbtCompound nbt) {
        return nbt.contains("DoorUuids");
    }
}
