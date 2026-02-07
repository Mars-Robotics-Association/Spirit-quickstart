# Spirit DECODE

Team Spirit's FTC robot code, forked from the [Road Runner quickstart](https://rr.brott.dev/docs/v1-0/tuning/). Everything below describes what the team built on top of commit `08f0898` (the upstream quickstart baseline).

## What Changed from the Quickstart

### Robot Subsystems

Subsystem and shared-logic classes in `teamcode.robot`:

- **Intake** -- Single DC motor (`intakeMotor`). Three intent-based methods: `run()`, `eject()`, and `stop()`.
- **Shooter** -- Dual flywheel motors (`shooterMotorLeft`, `shooterMotorRight`) plus a `tiltServo` that angles the shooter for near vs. far shots. Velocity control uses a hand-rolled feedforward + proportional feedback loop with exponential smoothing, rather than the SDK's built-in `RUN_USING_ENCODER` PID (which is commented out). Near target is ~650 tps, far target is ~925 tps.
- **Carousel** -- Two servos: `carouselServo` (continuous rotation, holds 3 balls at 120-degree spacing) and `kickerServo` (pushes balls into the shooter). Positions are computed from a degree-to-servo conversion factor with a configurable offset. Intake positions and launch positions are staggered so the carousel rotates to feed balls one at a time.
- **Lift** -- Encoder-driven motor (`liftMotor`) with `RUN_TO_POSITION` to raise the robot. Target is 1300 ticks.
- **LaunchSequence** -- Reusable 3-ball launch state machine shared by Teleop and all shooting autonomous routines. Encapsulates the timed sequence: flywheel ramp-up (RAMPING), per-ball carousel rotate → kicker nudge → tilt → fire loop (LAUNCHING), and cleanup phase (CLEANUP → DONE). Timing constants are tunable via FTC Dashboard.
- **AllianceSensor** -- REV Color/Distance Sensor V2 wrapper that compares red and blue channels to detect alliance color. Used by `AutoDetectAllianceNear`.

### Teleop: State Machine Launch Sequence

`Teleop2` (in `opmodes.teleop`) is the main driver-controlled OpMode. The launch sequence delegates to a shared `LaunchSequence` class (in `robot`) that both Teleop and the autonomous routines use:

1. Gamepad2 trigger pull starts the `LaunchSequence` — a 2-second flywheel ramp-up, then loops through 3 balls with sub-steps: rotate carousel → tiny kicker nudge → tilt shooter → full kicker fire → reset tilt and kicker, then a cleanup phase
2. Right trigger = near shot (lower tilt, lower velocity); left trigger = far shot (higher tilt, higher velocity)
3. The `LaunchSequence` resets to IDLE after completion, ready for reuse

Gamepad1's right trigger runs a separate 3-step intake sequence that advances the carousel through intake positions on successive pulls, with rising-edge detection to avoid repeat triggers.

### Autonomous Routines

Six autonomous OpModes (in `opmodes.auto`), all using timed drive commands (not Road Runner trajectories). The shooting OpModes share two abstract base classes (`BaseNearAuto`, `BaseFarAuto`) that handle hardware init, the `LaunchSequence`, and parking — leaving each concrete OpMode as a thin subclass that just sets the alliance color:

- **JustMove** -- Simplest: drives backward for 0.4 seconds to get off the starting tape for move points, then stops.
- **BlueNear / RedNear** -- Extend `BaseNearAuto`. Backs up 0.25s, executes the 3-ball near-shot `LaunchSequence`, then strafes to park against the field wall (direction and duration vary by alliance).
- **BlueFar / RedFar** -- Extend `BaseFarAuto`. Executes the 3-ball far-shot `LaunchSequence`, then drives forward 0.4s to park.
- **AutoDetectAllianceNear** -- Extends `BaseNearAuto` with an `onInit()` hook that uses an `AllianceSensor` (REV Color/Distance Sensor) to detect alliance color during init. Drivers see the detected color on telemetry before pressing Start.
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
8. **Continued refactoring** (Feb 2026) -- Replaced `Intake.setPower()` with intent-based `run()`/`eject()`/`stop()` API, fixed motor mode initialization for Lift and ShooterMotor, extracted the duplicated ~170-line launch state machine from 5 auto OpModes and Teleop into a shared `LaunchSequence` class (RAMPING → LAUNCHING → CLEANUP → DONE), introduced `BaseNearAuto` and `BaseFarAuto` abstract base classes to eliminate remaining autonomous duplication, and extracted `AllianceSensor` subsystem for color-sensor alliance detection
