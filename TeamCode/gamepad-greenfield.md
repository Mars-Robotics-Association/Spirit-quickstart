# Greenfield Gamepad Control Scheme

_A "what would be ideal from scratch" redesign — unconstrained by the current mapping._

This document complements [`gamepad-proposal.md`](gamepad-proposal.md), which fixes conflicts with minimal retraining. Here we start from zero and optimize purely for driving ergonomics and match performance.

---

## 1. Design Philosophy

### Driver thumbs never leave the sticks

Every millisecond a driver lifts a thumb to press A or D-pad is a millisecond the robot drifts uncontrolled. All driver actions live on triggers and bumpers — inputs you hit with your index fingers while your thumbs stay on the sticks.

### Driver owns driving + intake

Driving toward balls and intaking them is one fluid action. The driver should control both without waiting for the operator.

### Operator owns shooting + lift

Shooting and lifting are deliberate, high-stakes actions. The operator can focus entirely on timing and positioning these without worrying about driving.

### Analog triggers for analog actions, digital bumpers for toggles

Triggers are pressure-sensitive — use them for actions where "how much" matters (intake power, shot selection). Bumpers click on/off — use them for toggles (field-centric mode, pre-ramp).

### Pre-ramp the shooter

The current launch sequence spends ~2 seconds ramping flywheels before the first ball fires. If the operator can spin up early while the driver is still collecting, the first shot fires instantly when the trigger is pulled — saving ~2 seconds per cycle.

### Hold-to-raise lift

The lift raises while the button is held and lowers on release. It can never get stuck up because the operator forgot, and it doesn't require coordination across gamepads.

---

## 2. Match Flow Analysis

A typical teleop cycle looks like this:

```
  DRIVER                              OPERATOR
  ──────                              ────────
  Drive toward ball cluster           (idle — watching field)
  Hold RT to intake ball 1
  Hold RT to intake ball 2
  Hold RT to intake ball 3            RB tap — pre-ramp shooter flywheels
  Drive toward goal                   (flywheels spinning up during transit)
  Align with target                   Pull RT or LT — fire all 3 balls
  (back to collecting)                (sequence runs autonomously)

  ... repeat 4-6 times ...

  (endgame) Drive to bar              Hold A — lift robot off mat
  (hold position)                     (release A — robot lowers safely)
```

Key insight: the driver and operator are rarely busy at the same time. The driver is active during collection and transit; the operator is active during shooting and endgame. Pre-ramping bridges this gap — the operator starts flywheels while the driver is still driving.

---

## 3. Proposed Layout

### Gamepad 1 — Driver

```
              GAMEPAD 1 — DRIVER
    ┌──────────────────────────────────────┐
    │  [LB] hold           [RB] tap       │
    │  Slow mode            Toggle         │
    │  (momentary)          field-centric  │
    │  [LT] hold           [RT] hold      │
    │  Reverse intake       Intake +       │
    │  (eject)              Carousel       │
    │                       Advance        │
    │                                      │
    │    ┌───┐                ┌───┐        │
    │    │ L │ Drive          │ R │ Rotate │
    │    │   │ fwd/back       │   │(X axis)│
    │    │   │ + strafe       │   │        │
    │    └─┬─┘                └───┘        │
    │      │                               │
    │   [L-click]                          │
    │   Toggle crouch                      │
    │   (0.3× speed)                       │
    │                                      │
    │       [U]                            │
    │    [L]   [R]       [Y]               │
    │       [D]       [X]   [B]            │
    │                    [A]               │
    │    ── ALL UNUSED ──────────          │
    └──────────────────────────────────────┘
```

| Control              | Action                                 | Notes                                                 |
| -------------------- | -------------------------------------- | ----------------------------------------------------- |
| Left stick           | Drive fwd/back + strafe                |                                                       |
| Right stick X        | Rotate                                 | Absolute heading optional                             |
| Right trigger (hold) | Intake + carousel advance on each pull | Same as current — pull #1/2/3 cycles intake positions |
| Left trigger (hold)  | Reverse intake (eject)                 | Clears jams without operator help                     |
| Right bumper (tap)   | Toggle field-centric / robot-centric   | Visual indicator on Driver Station recommended        |
| Left bumper (hold)   | Momentary slow mode                    | Alternative to stick click — hold for fine adjustment |
| Face buttons         | **All unused**                         | Thumbs never leave sticks                             |
| D-pad                | **All unused**                         | Thumbs never leave sticks                             |

### Gamepad 2 — Operator

```
              GAMEPAD 2 — OPERATOR
    ┌──────────────────────────────────────┐
    │  [LB] tap            [RB] tap       │
    │  Abort launch         Pre-ramp      │
    │  sequence             shooter       │
    │  [LT]                [RT]           │
    │  Far shot             Near shot     │
    │  launch               launch        │
    │                                      │
    │    ┌───┐                ┌───┐        │
    │    │ L │                │ R │        │
    │    │   │ (unused)       │   │(unused)│
    │    │   │                │   │        │
    │    └───┘                └───┘        │
    │                                      │
    │       [U]                            │
    │    [L]   [R]       [Y]               │
    │       [D]       [X]   [B]            │
    │                    [A]               │
    │    (unused)     (unused) E-stop      │
    │                  Lift                │
    │                  (hold)              │
    └──────────────────────────────────────┘
```

| Control            | Action                                      | Notes                                               |
| ------------------ | ------------------------------------------- | --------------------------------------------------- |
| Right trigger      | Near-shot launch sequence                   | 650 ticks/sec, near tilt — skips ramp if pre-ramped |
| Left trigger       | Far-shot launch sequence                    | 925 ticks/sec, far tilt — skips ramp if pre-ramped  |
| Right bumper (tap) | Pre-ramp shooter                            | Spin flywheels early; saves ~2s on first shot       |
| Left bumper (tap)  | Abort launch sequence                       | Emergency cancel — stops shooter, homes servos      |
| A (hold)           | Lift — raises while held, lowers on release | Safe: can't get stuck up                            |
| B (tap)            | Emergency stop all mechanisms               | Kills shooter, intake, homes all servos             |
| Sticks             | **All unused**                              |                                                     |
| D-pad              | **All unused**                              |                                                     |
| X / Y              | **All unused**                              |                                                     |

---

## 4. Comparison: Current vs. Greenfield

| Capability           | Current Mapping    | Greenfield Mapping                    | Change            |
| -------------------- | ------------------ | ------------------------------------- | ----------------- |
| Drive fwd/back       | GP1 left stick Y   | GP1 left stick Y                      | Same              |
| Strafe               | GP1 left stick X   | GP1 left stick X                      | Same              |
| Rotate               | GP1 right stick X  | GP1 right stick X                     | Same              |
| Drive mode           | Robot-centric only | **Field-centric default** (toggle RB) | New               |
| Crouch / precision   | None               | **GP1 left stick click** (toggle)     | New               |
| Slow mode            | None               | **GP1 LB** (momentary hold)           | New               |
| Intake + carousel    | GP1 RT             | GP1 RT                                | Same              |
| Intake run (manual)  | GP2 LB             | Removed (driver has eject on LT)      | Simplified        |
| Intake eject         | GP2 RB             | **GP1 LT** (driver ejects own jams)   | Moved to driver   |
| Near shot sequence   | GP2 RT             | GP2 RT                                | Same              |
| Far shot sequence    | GP2 LT             | GP2 LT                                | Same              |
| Pre-ramp shooter     | None               | **GP2 RB**                            | New               |
| Abort launch         | None               | **GP2 LB**                            | New               |
| Lift up              | GP2 A (conflicted) | GP2 A (hold)                          | Fixed             |
| Lift down            | GP1 D-pad left     | GP2 A (release)                       | Moved to operator |
| Emergency stop       | None               | **GP2 B**                             | New               |
| 13 test controls     | Various GP1/GP2    | **All removed**                       | Cleaned           |
| Manual load sequence | GP2 A (conflicted) | **Removed**                           | Cleaned           |

**Summary:** 6 current capabilities preserved, 5 new capabilities added, 15 legacy/test mappings removed, 0 capabilities lost.

---

## 5. Implementation Notes

### Pre-Ramp Shooter

Modify `LaunchSequence` (or add a method to `Shooter`) to support a pre-ramp mode:

```java
// In Shooter or LaunchSequence:
public void preRamp(double velocity) {
    shooterMotorLeft.setTargetVelocity(velocity);
    shooterMotorRight.setTargetVelocity(velocity);
    isPreRamped = true;
}

// In LaunchSequence.start():
if (shooter.isPreRamped()) {
    state = State.LAUNCHING;  // Skip RAMPING state entirely
} else {
    state = State.RAMPING;
}
```

The pre-ramp spins at the near-shot velocity (650 ticks/sec) by default. When a trigger is pulled, if the requested velocity matches, the ramp phase is skipped entirely. If a far shot is requested but near was pre-ramped, a short ramp-up from 650 → 925 still occurs but takes less time than starting from zero.

### Abort Launch Sequence

Add a `cancel()` method to `LaunchSequence`:

```java
public void cancel() {
    shooter.stop();
    shooter.setHomeTiltPosition();
    carousel.setHomePositionKicker();
    state = State.IDLE;
    ballCount = 0;
}
```

### Hold-to-Raise Lift

Same approach as `gamepad-proposal.md` — no change needed:

```java
if (gamepad2.a) {
    lift.engageLift();
} else {
    lift.homeLift();
}
```

### Emergency Stop

```java
if (gamepad2.b) {
    shooter.stop();
    intake.stop();
    carousel.setHomePositionKicker();
    shooter.setHomeTiltPosition();
    launchSequence.cancel();
}
```

### Edge Detection for Toggles

Bumper taps and stick clicks need rising-edge detection to avoid toggling every loop iteration:

```java
// In loop():
boolean rbNow = gamepad1.right_bumper;
if (rbNow && !rbPrev) {
    driveHelper.toggleFieldCentric();
}
rbPrev = rbNow;

boolean lClickNow = gamepad1.left_stick_button;
if (lClickNow && !lClickPrev) {
    crouchOn = !crouchOn;
    driveHelper.setCrouch(crouchOn);
}
lClickPrev = lClickNow;
```

---

## 6. Optional Enhancements

These are "nice to have" ideas that build on the greenfield layout but aren't required for the core redesign.

### Rumble Feedback

The FTC SDK supports `gamepad.rumble()` on compatible controllers. Useful signals:

- **Short buzz on GP2** when flywheels reach target velocity (pre-ramp complete, ready to fire)
- **Double buzz on GP1** when all 3 intake slots are loaded (carousel full)
- **Long buzz on GP2** when launch sequence finishes (all 3 balls fired)

### Auto-Intake Mode

Instead of the driver manually pulling the trigger 3 times, a single trigger hold could automatically cycle through all 3 intake positions with a sensor-based "ball detected" trigger between each advance. Requires a sensor (color or distance) at the intake entry point.

### Heading Reset Button

If the IMU drifts during a match and field-centric becomes misaligned, one of the unused face buttons (e.g., GP1 Y) could reset the IMU heading to "forward = away from driver station." This keeps the thumbs-on-sticks principle for normal play but provides a recovery option.

### Analog Intake Power

Instead of binary on/off intake, the right trigger's analog value (0.0–1.0) could directly control intake motor power. Useful for gentle ball handling vs. aggressive collection, but may not be necessary given the current motor setup.
