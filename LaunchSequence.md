# LaunchSequence Flow

> **Source:** [`LaunchSequence.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/robot/LaunchSequence.java)

## Overview

`LaunchSequence` is a reusable, time-based state machine that fires all three balls from the carousel through the shooter. It is shared by both Teleop (`SpiritTeleop2`) and Autonomous (`BaseNearAuto`, `BaseFarAuto`) OpModes.

The sequence has four major phases: **Ramp-up**, **Launching** (repeated for 3 balls), **Cleanup**, and **Done**.

## State Machine Diagram

Open [`LaunchSequence.drawio`](LaunchSequence.drawio) in [draw.io](https://app.diagrams.net) or the VS Code *Draw.io Integration* extension to view the full state diagram.

## Tunable Parameters

All delays are `static public` fields exposed to FTC Dashboard via `@Config`:

| Parameter | Default | Description |
|---|---|---|
| `rampUpDuration` | 2.0 s | Time for flywheels to reach target velocity |
| `defaultStepDelay` | 1.0 s | Pause between most sub-steps |
| `tiltToLaunchDelay` | 1.0 s | Pause between tilting and firing the kicker |

## Phase-by-Phase Walkthrough

### 1. IDLE

The resting state. Nothing happens until `start(velocity, tiltPosition)` is called.

### 2. RAMPING

```
start(velocity, tiltPos)
  → sets shooter target velocity
  → calls shooter.update() every loop to spin up flywheels
  → waits rampUpDuration (2.0s)
  → homes tilt servo and kicker servo
  → transitions to LAUNCHING
```

The shooter's feedforward + feedback velocity controller runs each loop iteration during ramp-up, letting the flywheels smoothly reach the target speed.

### 3. LAUNCHING (repeats for balls 0, 1, 2)

Each ball goes through five sub-steps. After ball 2's kick, it skips the reset and goes straight to CLEANUP.

| Sub-step | Action | Wait before acting | Hardware call |
|---|---|---|---|
| **0** | Rotate carousel to ball N's launch slot | `defaultStepDelay` | `carousel.spinCarouselLaunchOne/Two/Three()` |
| **1** | Tiny kicker lift (seats ball against flywheel) | `defaultStepDelay` (+0.2s extra for ball 0) | `carousel.setTinyKicker()` |
| **2** | Tilt shooter to launch angle | `defaultStepDelay` | `shooter.setTiltPosition(tiltPosition)` |
| **3** | Full kicker (fires the ball!) | `tiltToLaunchDelay` | `carousel.setFullKicker()` |
| **4** | Reset tilt + kicker for next ball | `defaultStepDelay` | `shooter.setHomeTiltPosition()` + `carousel.setHomePositionKicker()` |

After sub-step 4, `ballNumber` increments and the loop restarts at sub-step 0. Sub-step 4 is **skipped** for ball 2 — instead, sub-step 3 transitions directly to CLEANUP.

Ball 0 gets an extra 0.2 s delay at the tiny-kicker step (sub-step 1) to allow additional settling time after the first carousel rotation.

### 4. CLEANUP

Returns all servos to safe positions and stops the shooter:

| Sub-step | Action | Wait before acting | Hardware call |
|---|---|---|---|
| **0** | Home the tilt servo | `defaultStepDelay + 0.2s` | `shooter.setHomeTiltPosition()` |
| **1** | Home the kicker servo | `defaultStepDelay + 0.5s` | `carousel.setHomePositionKicker()` |
| **2** | Reset carousel to launch-one position, stop flywheels | `defaultStepDelay + 0.5s` | `carousel.spinCarouselLaunchOne()` + `shooter.shooterVelocity = 0` |

### 5. DONE

The terminal state. `isDone()` returns `true`. The caller can invoke `reset()` to return to IDLE for reuse (e.g., firing a second volley in Teleop).

## Approximate Total Duration

With default timing (`defaultStepDelay = 1.0`, `tiltToLaunchDelay = 1.0`, `rampUpDuration = 2.0`):

| Phase | Duration |
|---|---|
| Ramp-up | 2.0 s |
| Ball 0 (sub-steps 0-4) | 5.2 s (extra 0.2s at tiny kicker) |
| Ball 1 (sub-steps 0-4) | 5.0 s |
| Ball 2 (sub-steps 0-3, no reset) | 4.0 s |
| Cleanup (sub-steps 0-2) | 3.2 s |
| **Total** | **~19.4 s** |

## Usage

```java
LaunchSequence launch = new LaunchSequence(shooter, carousel, this::getRuntime);

// Trigger the sequence (e.g., on button press):
launch.start(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);

// In the main loop:
launch.update();
if (launch.isDone()) {
    launch.reset();  // ready for next use
}

// Guard other controls while launching:
if (!launch.isRunning()) {
    // safe to accept manual shooter/carousel input
}
```
