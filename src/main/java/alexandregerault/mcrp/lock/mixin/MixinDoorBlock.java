package alexandregerault.mcrp.lock.mixin;

import alexandregerault.mcrp.MinecraftRolePlay;
import alexandregerault.mcrp.lock.LockableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock extends Block implements BlockEntityProvider {
    private static final BooleanProperty LOCKED = Properties.LOCKED;
    public MixinDoorBlock(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void mcrpDoorBlock(Settings settings, CallbackInfo ci) {
        this.setDefaultState(this.stateManager.getDefaultState().with(LOCKED, false));
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    public void mcrpAppendLockedProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(LOCKED);
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
        if (state.get(LOCKED)) {
            player.sendMessage(new TranslatableText("block.mcrp.door.locked"), true);
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LockableBlockEntity(pos, state);
    }
}
