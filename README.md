# Spirit DECODE

Team Spirit's FTC robot code, forked from the [Road Runner quickstart](https://rr.brott.dev/docs/v1-0/tuning/). Everything below describes what the team built on top of commit `08f0898` (the upstream quickstart baseline).

## What Changed from the Quickstart

### Robot Subsystems

Four new hardware subsystem classes were added in `teamcode.robot`:

- **Intake** -- Single DC motor (`intakeMotor`). Thin wrapper: just `setPower()`.
- **Shooter** -- Dual flywheel motors (`shooterMotorLeft`, `shooterMotorRight`) plus a `tiltServo` that angles the shooter for near vs. far shots. Velocity control uses a hand-rolled feedforward + proportional feedback loop with exponential smoothing, rather than the SDK's built-in `RUN_USING_ENCODER` PID (which is commented out). Near target is ~650 tps, far target is ~925 tps.
- **Carousel** -- Two servos: `carouselServo` (continuous rotation, holds 3 balls at 120-degree spacing) and `kickerServo` (pushes balls into the shooter). Positions are computed from a degree-to-servo conversion factor with a configurable offset. Intake positions and launch positions are staggered so the carousel rotates to feed balls one at a time.
- **Lift** -- Encoder-driven motor (`liftMotor`) with `RUN_TO_POSITION` to raise the robot. Target is 1300 ticks.

### Teleop: State Machine Launch Sequence

`Teleop2` (in `opmodes.teleop`) is the main driver-controlled OpMode. The most interesting part is the timed state machine that fires all three balls from the carousel using a loop with sub-steps:

1. Gamepad2 trigger pull enters **RAMPING** state (2-second flywheel spin-up)
2. Transitions to **LAUNCHING**, which loops through `ballNumber` 0-2, each with 5 sub-steps: rotate carousel to launch position → tiny kicker nudge → tilt shooter → full kicker fire → reset tilt and kicker
3. Right trigger = near shot (lower tilt, lower velocity); left trigger = far shot (higher tilt, higher velocity)
4. After all 3 balls are fired, a cleanup phase homes the tilt, kicker, and carousel, then returns to **IDLE**

Gamepad1's right trigger runs a separate 3-step intake sequence that advances the carousel through intake positions on successive pulls, with rising-edge detection to avoid repeat triggers.

### Autonomous Routines

Six autonomous OpModes (in `opmodes.auto`), all using timed drive commands (not Road Runner trajectories):

- **JustMove** -- Simplest: drives backward for 0.4 seconds to get off the starting tape for move points, then stops.
- **BlueNear / RedNear** -- Backs up, executes the full 3-ball launch sequence at near-shot settings, then strafes to park against the field wall.
- **BlueFar / RedFar** -- Same pattern but with far-shot velocity and tilt.
- **AutoDetectAllianceNear** -- Uses a REV Color/Distance Sensor to detect alliance color during init, then runs the near-shot sequence and strafes in the correct direction.
- **TestingEncodersBlue** -- An experimental encoder-based autonomous (marked `@Disabled`) that drives by encoder tick counts instead of time. Uses `COUNTS_PER_INCH` conversion with 751.8 ticks/rev and 4-inch wheels.

## Development Timeline

The commit history tells the story of iterative hardware bring-up:

1. **Initial bring-up** (Nov 2025) -- Drive + intake working, then sensor samples added
2. **Bug fixes** -- A series of "compare not assign" commits (likely `=` vs `==` bugs in conditionals)
3. **State machine rewrite** (Dec 2025) -- Moved from inline logic to the IDLE/RAMPING/LAUNCHING state engine; several reverts and do-overs as the state machine was debugged ("if the cases are commented out, driving & shooting do not work")
4. **Servo tuning** (Dec 2025-Jan 2026) -- Carousel positions, kicker lift heights, and tilt angles refined through iterative testing
5. **Autonomous development** (Jan 2026) -- Started with teleop-converted autonomous, then split into color/distance variants
6. **Competition refinement** (Jan-Feb 2026) -- "Reverted to code used at Seneca Valley", tuned tilt and speed, added lift mechanism
7. **Code review & cleanup** (Feb 2026) -- Fixed lift homing bug, added Javadoc across all subsystems and OpModes, refactored the 16-step launch state machine into a loop with sub-steps, extracted `ShooterMotor` class with power quantization and separate target/actual smoothing factors, removed dead code and redundant comments, reorganized OpModes into `opmodes.auto` and `opmodes.teleop` packages with concise class names
