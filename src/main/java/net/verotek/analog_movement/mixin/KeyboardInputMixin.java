package net.verotek.analog_movement.mixin;

//? if <1.21.3 {
import net.minecraft.client.player.Input;
//? } else {
/*import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
*///? }
//? if >=1.21.5 {
/*import net.minecraft.world.phys.Vec2;
*///? }
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.verotek.libanalog.interfaces.mixin.IAnalogKeybinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.client.input.KeyboardInput")
public abstract class KeyboardInputMixin extends /*? if >=1.21.3 {*/ /*ClientInput *//*?} else {*/ Input /*?}*/ {
  @Shadow @Final private Options options;

  @Unique
  private float computeSidewaysMovement(KeyMapping positive, KeyMapping negative) {
    IAnalogKeybinding analogPositive = (IAnalogKeybinding) positive;
    IAnalogKeybinding analogNegative = (IAnalogKeybinding) negative;

    return analogPositive.pressedAmount() - analogNegative.pressedAmount();
  }

  @Unique
  private float computeForwardMovement(KeyMapping positive, KeyMapping negative) {
    IAnalogKeybinding analogPositive = (IAnalogKeybinding) positive;
    IAnalogKeybinding analogNegative = (IAnalogKeybinding) negative;

    if (options.keySprint.isDown()
        && positive.isDown()
        && analogPositive.pressedAmount() > analogNegative.pressedAmount()) {
      return 1.0f;
    }

    return analogPositive.pressedAmount() - analogNegative.pressedAmount();
  }

  /**
   * @author lvoegl
   * @reason Computes player movement based on analog inputs.
   */
  @Overwrite
  public void tick(/*? if <1.21.4 {*/ boolean slowDown, float slowDownFactor /*?}*/) {
    Screen screen = Minecraft.getInstance().screen;
    LocalPlayer player = Minecraft.getInstance().player;
    if (screen != null || player == null) {
      // reset all movement

      //? if >=1.21.3 {
      /*keyPresses = new Input(false, false, false, false, false, false, false);
      *///?} else {
      up = false;
      down = false;
      left = false;
      right = false;
      jumping = false;
      shiftKeyDown = false;
      //?}

      //? if >=1.21.5 {
      /*moveVector = new Vec2(0.0f, 0.0f);
      *///?} else {
      forwardImpulse = 0.0f;
      leftImpulse = 0.0f;
      //?}
      return;
    }

    float forwardMovement = computeForwardMovement(options.keyUp, options.keyDown);
    float sidewaysMovement = computeSidewaysMovement(options.keyLeft, options.keyRight);

    //? if >=1.21.3 {
    /*keyPresses = new Input(
        options.keyUp.isDown(),
        options.keyDown.isDown(),
        options.keyLeft.isDown(),
        options.keyRight.isDown(),
        options.keyJump.isDown(),
        options.keyShift.isDown(),
        options.keySprint.isDown());
    *///?} else {
    up = options.keyUp.isDown();
    down = options.keyDown.isDown();
    left = options.keyLeft.isDown();
    right = options.keyRight.isDown();
    jumping = options.keyJump.isDown();
    shiftKeyDown = options.keyShift.isDown();
    //?}

    //? if <1.21.4 {
    if (slowDown) {
      forwardMovement *= slowDownFactor;
      sidewaysMovement *= slowDownFactor;
    }
    //?}

    //? if >=1.21.5 {
    /*moveVector = new Vec2(sidewaysMovement, forwardMovement);
    *///?} else {
    forwardImpulse = forwardMovement;
    leftImpulse = sidewaysMovement;
    //?}
  }
}
