package alexandregerault.mcrp.lock;

import alexandregerault.mcrp.MinecraftRolePlay;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Lock {
    public static BlockEntityType<LockableBlockEntity> LOCKABLE_BLOCK_ENTITY;

    public void run() {
        LOCKABLE_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(MinecraftRolePlay.MOD_ID, "lockable_block_entity"),
                FabricBlockEntityTypeBuilder.create(
                        LockableBlockEntity::new,
                        Blocks.OAK_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.BIRCH_DOOR,
                        Blocks.CRIMSON_DOOR, Blocks.IRON_DOOR, Blocks.JUNGLE_DOOR, Blocks.SPRUCE_DOOR,
                        Blocks.WARPED_DOOR
                ).build(null)
        );
    }
}
