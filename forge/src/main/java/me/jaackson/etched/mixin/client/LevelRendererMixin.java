package me.jaackson.etched.mixin.client;

import me.jaackson.etched.client.sound.StopListeningSound;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Unique
    private BlockPos pos;

    @Shadow
    private ClientLevel level;

    @Shadow
    protected abstract void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl);

    @Inject(method = "playRecord", at = @At("HEAD"))
    public void playRecord(SoundEvent soundEvent, BlockPos pos, RecordItem musicDiscItem, CallbackInfo ci) {
        this.pos = pos;
    }

    @ModifyVariable(method = "playRecord", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE), index = 4)
    public SoundInstance modifySoundInstance(SoundInstance soundInstance) {
        return new StopListeningSound(soundInstance, () -> this.notifyNearbyEntities(this.level, this.pos, false));
    }
}
