package alexandregerault.mcrp.lock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

public class LockableBlockEntity extends BlockEntity {
    private UUID uuid;
    private boolean freeOfKey = true;
    private boolean locked = false;

    public LockableBlockEntity(BlockPos pos, BlockState state) {
        super(Lock.LOCKABLE_BLOCK_ENTITY, pos, state);
        uuid = UUID.randomUUID();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putUuid("UUID", uuid);
        tag.putBoolean("FreeOfKey", freeOfKey);
        tag.putBoolean("Locked", locked);

        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        uuid = tag.getUuid("UUID");
        freeOfKey = tag.getBoolean("FreeOfKey");
        locked = tag.getBoolean("Locked");
    }

    public UUID uuid() {
        return uuid;
    }

    public void lock() {
        locked = true;
        freeOfKey = false;
    }

    public void unlock() {
        locked = false;
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
