package alexandregerault.mcrp.lock;

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

        if (world.isClient || isNotLockable(blockEntity) || player == null) {
            return super.useOnBlock(context);
        }

        LockableBlockEntity upperBlockEntity = (LockableBlockEntity) (world.getBlockState(context.getBlockPos()).get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                ? world.getBlockEntity(context.getBlockPos())
                : world.getBlockEntity(context.getBlockPos().offset(Direction.Axis.Y, 1)));
        LockableBlockEntity lowerBlockEntity = (LockableBlockEntity) (world.getBlockState(context.getBlockPos()).get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER)
                ? world.getBlockEntity(context.getBlockPos())
                : world.getBlockEntity(context.getBlockPos().offset(Direction.Axis.Y, -1)));

        if (upperBlockEntity == null || lowerBlockEntity == null) {
            return super.useOnBlock(context);
        }

        ItemStack key = context.getStack();
        NbtCompound nbt = key.getNbt();

        if (nbt == null) {
            return lockDoor(context, key, player, upperBlockEntity, lowerBlockEntity);
        }

        if (isRecordedBlockEntity(nbt, upperBlockEntity) && isRecordedBlockEntity(nbt, lowerBlockEntity)) {
            return upperBlockEntity.isLocked()
                    ? unlockDoor(context, key, player, upperBlockEntity, lowerBlockEntity)
                    : lockDoor(context, key, player, upperBlockEntity, lowerBlockEntity);
        } else if (!hasAlreadyLockedDoor(nbt)) {
            return lockDoor(context, key, player, upperBlockEntity, lowerBlockEntity);
        }

        player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_door"), true);
        return super.useOnBlock(context);
    }

    private ActionResult lockDoor(ItemUsageContext context, ItemStack key, PlayerEntity player, LockableBlockEntity... blockEntities) {
        NbtCompound nbt = key.getOrCreateNbt();

        if (!nbt.contains("uuid")) {
            UUID keyId = UUID.randomUUID();
            nbt.putUuid("uuid", keyId);
        }

        boolean shouldKeepProcessing = true;
        for (LockableBlockEntity blockEntity : blockEntities) {
            if (shouldKeepProcessing) {
                shouldKeepProcessing = blockEntity.lock(nbt.getUuid("uuid"), player);
            }
        }

        if (!shouldKeepProcessing) {
            return super.useOnBlock(context);
        }

        this.writeNbt(nbt, blockEntities);
        player.sendMessage(new TranslatableText("item.mcrp.key_item.locking"), true);
        return super.useOnBlock(context);
    }

    private ActionResult unlockDoor(ItemUsageContext context, ItemStack key, PlayerEntity player, LockableBlockEntity... blockEntities) {
        NbtCompound nbt = key.getOrCreateNbt();

        boolean shouldKeepProcessing = true;
        for (LockableBlockEntity blockEntity : blockEntities) {
            if (shouldKeepProcessing) {
                shouldKeepProcessing = blockEntity.unlock(nbt.getUuid("uuid"), player);
            }
        }

        if (!shouldKeepProcessing) {
            return super.useOnBlock(context);
        }

        this.writeNbt(nbt, blockEntities);
        player.sendMessage(new TranslatableText("item.mcrp.key_item.unlocking"), true);
        return super.useOnBlock(context);
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
        for (LockableBlockEntity target : targets) {
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
