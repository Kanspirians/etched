package me.jaackson.etched.mixin.client;

import me.jaackson.etched.common.item.BoomboxItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow
    public ModelPart leftArm;

    @Shadow
    public ModelPart rightArm;

    // TODO: fix arm swing when holding a boombox

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    public void poseRightArm(T livingEntity, CallbackInfo ci) {
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            if ((player.getMainArm() == HumanoidArm.RIGHT && BoomboxItem.isPlaying(player.getMainHandItem())) ||
                    (player.getMainArm() == HumanoidArm.LEFT && BoomboxItem.isPlaying(player.getOffhandItem()))) {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
                this.rightArm.yRot = 0.0F;
                this.rightArm.zRot = -0.610865F;
                ci.cancel();
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    public void poseLeftArm(T livingEntity, CallbackInfo ci) {
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            if ((player.getMainArm() == HumanoidArm.LEFT && BoomboxItem.isPlaying(player.getMainHandItem())) ||
                    (player.getMainArm() == HumanoidArm.RIGHT && BoomboxItem.isPlaying(player.getOffhandItem()))) {
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
                this.leftArm.yRot = 0.0F;
                this.leftArm.zRot = 0.610865F;
                ci.cancel();
            }
        }
    }

}
