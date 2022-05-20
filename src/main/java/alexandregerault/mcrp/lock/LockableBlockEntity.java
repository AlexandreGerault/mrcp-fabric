package alexandregerault.mcrp.lock;

import alexandregerault.mcrp.MinecraftRolePlay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class LockableBlockEntity extends BlockEntity {
    private UUID uuid;

    @Nullable
    private UUID key = null;
    private boolean locked = false;

    public LockableBlockEntity(BlockPos pos, BlockState state) {
        super(Lock.LOCKABLE_BLOCK_ENTITY, pos, state);
        uuid = UUID.randomUUID();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putUuid("uuid", uuid);
        if (key != null) {
            tag.putUuid("key", key);
        }
        tag.putBoolean("locked", locked);

        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        uuid = tag.getUuid("uuid");
        if (tag.contains("key")) {
            key = tag.getUuid("key");
        }
        locked = tag.getBoolean("locked");
    }

    public UUID uuid() {
        return uuid;
    }

    public boolean lock(UUID keyUuid, PlayerEntity player) {
        World world = getWorld();

        MinecraftRolePlay.LOGGER.info("Key is not null? {} Are not equals? {}", key != null, !keyUuid.equals(key));

        if (key != null && !keyUuid.equals(key)) {
            player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_key"), true);
            return false;
        }

        if (world != null) {
            world.setBlockState(this.pos, world.getBlockState(this.pos).with(Properties.LOCKED, true));
            locked = true;
            key = keyUuid;
        }

        return true;
    }

    public boolean unlock(UUID keyUuid, PlayerEntity player) {
        World world = getWorld();

        if (key != null && !keyUuid.equals(key)) {
            player.sendMessage(new TranslatableText("item.mcrp.key_item.wrong_key"), true);
            return false;
        }

        if (world != null) {
            world.setBlockState(this.pos, world.getBlockState(this.pos).with(Properties.LOCKED, false));
            locked = false;
        }

        return true;
    }

    public boolean isLocked() {
        return locked;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
