# Gamepad Control Scheme Proposal

## 1. Current Problems

### Gamepad2 A Button Conflict
Pressing `gamepad2.a` triggers **two unrelated actions simultaneously**:
- **Line 86**: `lift.engageLift()` — raises the lift to 1300 ticks
- **Line 219**: Manual load sequence — homes kicker, spins carousel to launch position 1, sets tiny kicker

These fire every loop iteration while A is held, causing the lift motor and carousel servo to fight each other for priority.

### Lift Controls Split Across Gamepads
- **Lift UP**: `gamepad2.a` (operator)
- **Lift DOWN**: `gamepad1.dpad_left` (driver) — calls `lift.homeLift()`

The driver shouldn't need to manage the lift. Both directions should be on the same gamepad.

### 12+ Testing-Only Controls in Competition Layout
These buttons exist solely for servo/motor tuning during development and have no use in competition:

| Button | Action | Purpose |
|--------|--------|---------|
| `gamepad1.a` | `shooter.setNearTiltPosition()` | Tilt test |
| `gamepad1.b` | `shooter.setFarTiltPosition()` | Tilt test |
| `gamepad1.x` | `shooter.setHomeTiltPosition()` | Tilt test |
| `gamepad1.dpad_up` | `carousel.spinCarouselLaunchOne()` | Carousel position test |
| `gamepad1.dpad_right` | `carousel.spinCarouselLaunchTwo()` | Carousel position test |
| `gamepad1.dpad_down` | `carousel.spinCarouselLaunchThree()` | Carousel position test |
| `gamepad2.b` | `carousel.setHomePositionKicker()` | Kicker test |
| `gamepad2.x` | `carousel.setTinyKicker()` | Kicker test |
| `gamepad2.y` | `carousel.setFullKicker()` | Kicker test |
| `gamepad2.dpad_left` | `carousel.spinCarouselHome()` | Carousel position test |
| `gamepad2.dpad_up` | `carousel.spinCarouselIntakeOne()` | Carousel position test |
| `gamepad2.dpad_right` | `carousel.spinCarouselIntakeTwo()` | Carousel position test |
| `gamepad2.dpad_down` | `carousel.spinCarouselIntakeThree()` | Carousel position test |

These clutter the layout and create accidental-press risk during competition.

### Duplicate `setHomePositionKicker()` Call
The manual load sequence (line 219-224) calls `carousel.setHomePositionKicker()` twice in a row — likely a copy-paste error.

### Intake on Both Gamepads
- `gamepad1.right_trigger` runs the intake (as part of the intake+carousel sequence)
- `gamepad2.left_bumper` / `gamepad2.right_bumper` also control the intake

Two people can send conflicting commands to the same motor.

---

## 2. Proposed Layout

### Gamepad 1 — Driver (driving + intake)

```
              GAMEPAD 1 — DRIVER
    ┌──────────────────────────────────────┐
    │  [LB]              [RB]             │
    │  (unused)           (unused)         │
    │  [LT]              [RT]             │
    │  (unused)           Intake +         │
    │                     Carousel Advance │
    │                                      │
    │    ┌───┐              ┌───┐          │
    │    │ L │ Drive        │ R │ Rotate   │
    │    │   │ fwd/back     │   │ (X axis) │
    │    │   │ + strafe     │   │          │
    │    └───┘              └───┘          │
    │                                      │
    │       [U]                            │
    │    [L]   [R]     [Y]                 │
    │       [D]     [X]   [B]              │
    │    (all unused)  [A]                 │
    │                  (all unused)         │
    └──────────────────────────────────────┘
```

| Control | Action |
|---------|--------|
| Left stick Y | Drive forward / backward |
| Left stick X | Strafe left / right |
| Right stick X | Rotate |
| Right trigger (hold) | Run intake; each press advances carousel (pos 1 → 2 → 3 → 1) |

Everything else on gamepad 1 is **unused** — the driver only drives and collects balls.

### Gamepad 2 — Operator (all mechanisms)

```
              GAMEPAD 2 — OPERATOR
    ┌──────────────────────────────────────┐
    │  [LB]              [RB]             │
    │  Intake Run         Intake Eject     │
    │  [LT]              [RT]             │
    │  Far Shot           Near Shot        │
    │  Sequence           Sequence         │
    │                                      │
    │    ┌───┐              ┌───┐          │
    │    │ L │              │ R │          │
    │    │   │ (unused)     │   │ (unused) │
    │    │   │              │   │          │
    │    └───┘              └───┘          │
    │                                      │
    │       [U]                            │
    │    [L]   [R]     [Y]                 │
    │       [D]     [X]   [B]              │
    │    (all unused)  [A]                 │
    │                  Lift (hold)          │
    │                  (others unused)      │
    └──────────────────────────────────────┘
```

| Control | Action |
|---------|--------|
| Left bumper (hold) | Intake run (forward) |
| Right bumper (hold) | Intake eject (reverse) |
| Left trigger | Far shot launch sequence (925 ticks/sec, far tilt) |
| Right trigger | Near shot launch sequence (650 ticks/sec, near tilt) |
| A (hold) | Lift — raises while held, lowers on release |

Everything else on gamepad 2 is **unused** — clean layout with no accidental-press risk.

---

## 3. Complete Function Coverage

Every production function has exactly one mapping, no conflicts, no duplicates:

| Function | Current Mapping(s) | Proposed Mapping |
|----------|-------------------|-----------------|
| Drive fwd/back | GP1 left stick Y | GP1 left stick Y |
| Strafe | GP1 left stick X | GP1 left stick X |
| Rotate | GP1 right stick X | GP1 right stick X |
| Intake + carousel advance | GP1 right trigger | GP1 right trigger |
| Intake run (manual) | GP2 left bumper | GP2 left bumper |
| Intake eject (manual) | GP2 right bumper | GP2 right bumper |
| Near shot sequence | GP2 right trigger | GP2 right trigger |
| Far shot sequence | GP2 left trigger | GP2 left trigger |
| Lift up | GP2 A (conflicted) | GP2 A (hold) |
| Lift down | GP1 dpad left | GP2 A (release) |
| Manual load | GP2 A (conflicted) | **Removed** |
| 13 test controls | Various | **All removed** |

---

## 4. Design Rationale

### Driver focuses on driving, operator focuses on mechanisms
The driver's only non-driving responsibility is the intake+carousel sequence, which is naturally tied to driving because you drive toward the balls. All shooting, lifting, and manual mechanism control stays on gamepad 2.

### Related functions grouped together
- Triggers = shooting (left = far, right = near — mnemonic: **r**ight trigger = **n**ear, matching the shorter distance)
- Bumpers = raw intake control (left = run, right = eject)
- A = lift

### Hold-to-raise lift is inherently safe
The current `engageLift()` fires the motor to position 1300 and it stays there until someone presses `dpad_left` on gamepad 1. If the driver forgets or is busy, the lift stays raised. With hold-to-raise, the lift comes down the instant the operator releases the button — it can never get stuck up. Implementation: call `engageLift()` while `gamepad2.a` is true, call `homeLift()` when it becomes false.

### Testing controls removed — use FTC Dashboard instead
All 13 testing controls (tilt positions, kicker positions, carousel positions) are better served by FTC Dashboard's `@Config` live tuning. Dashboard lets you adjust servo positions with sliders rather than fixed button-to-position mappings, and it doesn't consume competition button real estate.

### Manual load removed
The manual load sequence (home kicker → carousel to launch 1 → tiny kicker) was a debugging shortcut. The launch state machine already handles ball loading automatically. If manual loading is needed during competition, it can be added back on a d-pad button.

---

## 5. Open Questions for Team Discussion

1. **Should intake run/eject stay on gamepad 2 bumpers?** The driver already has intake on right trigger via the advance sequence. Having it on both gamepads was flagged as a problem — but bumpers on GP2 could be useful for the operator to clear jams without the driver stopping.

2. **Do we need a manual carousel home button?** Currently `gamepad2.dpad_left` homes the carousel. This could be useful if the launch sequence gets stuck, but it's also an accidental-press risk.

3. **Lift hold behavior timing** — should there be a minimum hold time before the lift starts lowering on release? This prevents accidental tap-and-drop.
