package alexandregerault.mcrp.lock.mixin;

import alexandregerault.mcrp.MinecraftRolePlay;
import alexandregerault.mcrp.lock.LockableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock extends Block implements BlockEntityProvider {
    public MixinDoorBlock(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    public void mcrpPreventUseIfLocked(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        BlockEntity target = world.getBlockEntity(pos);

        if (!(target instanceof LockableBlockEntity lockableBlockEntity)) {
            return;
        }

        MinecraftRolePlay.LOGGER.info("Is locked? {}", lockableBlockEntity.isLocked());
        if (lockableBlockEntity.isLocked()) {
            player.sendMessage(new TranslatableText("block.mcrp.door.locked"), true);
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LockableBlockEntity(pos, state);
    }
}
