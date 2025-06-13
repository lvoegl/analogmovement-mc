package net.verotek.analog_movement.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
//? if >=1.21.3 {
/*import net.minecraft.util.PlayerInput;
*///?}
import net.verotek.libanalog.interfaces.mixin.IAnalogKeybinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
  @Shadow @Final private GameOptions settings;

  @Unique
  private float computeSidewaysMovement(KeyBinding positive, KeyBinding negative) {
    IAnalogKeybinding analogPositive = (IAnalogKeybinding) positive;
    IAnalogKeybinding analogNegative = (IAnalogKeybinding) negative;

    return analogPositive.pressedAmount() - analogNegative.pressedAmount();
  }

  @Unique
  private float computeForwardMovement(KeyBinding positive, KeyBinding negative) {
    IAnalogKeybinding analogPositive = (IAnalogKeybinding) positive;
    IAnalogKeybinding analogNegative = (IAnalogKeybinding) negative;

    if (settings.sprintKey.isPressed()
        && positive.isPressed()
        && analogPositive.pressedAmount() > analogNegative.pressedAmount()) {
      return 1.0f;
    }

    return analogPositive.pressedAmount() - analogNegative.pressedAmount();
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  //? if >=1.21.4 {
  /*private void tick(CallbackInfo ci) {
  *///?} else {
  private void tick(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
  //?}
    float forwardMovement = computeForwardMovement(settings.forwardKey, settings.backKey);
    float sidewaysMovement = computeSidewaysMovement(settings.leftKey, settings.rightKey);

    //? if >=1.21.3 {
    /*playerInput =
        new PlayerInput(
            forwardMovement > 0.0f,
            forwardMovement < 0.0f,
            sidewaysMovement > 0.0f,
            sidewaysMovement < 0.0f,
            settings.jumpKey.isPressed(),
            settings.sneakKey.isPressed(),
            settings.sprintKey.isPressed());
    *///?} else {
    pressingForward = forwardMovement > 0.0f;
    pressingBack = forwardMovement < 0.0f;
    pressingLeft = sidewaysMovement > 0.0f;
    pressingRight = sidewaysMovement < 0.0f;

    jumping = settings.jumpKey.isPressed();
    sneaking = settings.sneakKey.isPressed();
    //?}

    //? if <1.21.4 {
    if (slowDown) {
      forwardMovement *= slowDownFactor;
      sidewaysMovement *= slowDownFactor;
    }
    //?}

    movementForward = forwardMovement;
    movementSideways = sidewaysMovement;
    ci.cancel();
  }
}
