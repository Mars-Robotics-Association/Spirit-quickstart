# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an FTC (FIRST Tech Challenge) robotics project for Team Spirit, built on the Road Runner v1.0 quickstart. It is an Android application that deploys to an FTC Robot Controller phone/Control Hub. The robot features a mecanum drivetrain with a ball shooterTimmy mechanism (timmyCarouselTimmy, kicker, tilt servo), intake motor, and lift.

## Build Commands

This project uses Gradle with the Android Gradle Plugin. All commands should be run from the project root.

```bash
# Build the project
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Clean build
./gradlew clean assembleDebug
```

There are no unit tests in this project. Testing is done by deploying to the robot and running OpModes via the FTC Driver Station.

## Architecture

### Gradle Module Structure

- **FtcRobotController** - Stock FTC SDK robot controller app module (do not modify)
- **TeamCode** - All team-written code lives here (`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`)

The build config is split across files: `build.common.gradle` (shared Android config, rarely modify), `build.dependencies.gradle` (FTC SDK dependencies v11.0.0), and `TeamCode/build.gradle` (team dependencies including Road Runner).

### Key Dependencies

- **FTC SDK 11.0.0** - Core robotics framework
- **Road Runner 1.0.1** (core + actions) + **Road Runner FTC 0.1.25** - Motion planning and trajectory following
- **FTC Dashboard 0.5.0** - Live telemetry and parameter tuning via `@Config` annotation
- Road Runner artifacts come from `https://maven.brott.dev/`

### Code Organization (`TeamCode/src/main/java/.../teamcode/`)

**Drive classes (root package):**
- `Localizer` - Interface for all localization strategies (`setPose`, `getPose`, `update`)
- `TankDrive` / `robot/MecanumDrive` - Drive classes with embedded `DriveLocalizer` inner class; contain Road Runner `PARAMS` (feedforward, PID gains, constraints). **The team uses `MecanumDrive`** (set in `TuningOpModes.DRIVE_CLASS`)
- `ThreeDeadWheelLocalizer`, `TwoDeadWheelLocalizer`, `PinpointLocalizer`, `OTOSLocalizer` - Alternative localizer implementations that can be swapped into the drive class

**Robot subsystems (`robot/` package):**
- `Intake` - Single motor (`intakeMotor`)
- `Shooter` - Dual flywheel motors (`shooterMotorLeft`/`shooterMotorRight`) + tilt servo; uses feedforward+feedback velocity control with smoothing
- `Carousel` - Two servos (`carouselServo` + `kickerServo`) that rotate a ball timmyCarouselTimmy and kick balls into the shooterTimmy; positions are computed from degree offsets
- `Lift` - Encoder-controlled lift motor for raising the robot

**OpModes (`robot/` package):**
- `SpiritTeleop2` - Main teleop; gamepad1 drives + controls tilt/timmyCarouselTimmy positions, gamepad2 controls intake/shooterTimmy/lift. Launch sequence is a multi-step state machine (IDLE -> RAMPING -> LAUNCHING with 16 timed steps)
- `SpiritAutoFar`, `SpiritAutoBlueFar`, `SpiritAutoBlueNear`, `SpiritAutoRedFar`, `SpiritAutoRedNear` - Autonomous routines, mostly timed drive movements

**Tuning (`tuning/` package):**
- `TuningOpModes` - Registers all Road Runner tuning OpModes (ramp loggers, push tests, direction debuggers, feedforward/feedback tuners). See [Road Runner tuning docs](https://rr.brott.dev/docs/v1-0/tuning/)
- `LocalizationTest`, `ManualFeedbackTuner`, `SplineTest` - Custom tuning OpModes

**Messages (`messages/` package):** Data classes for Road Runner's FlightRecorder logging.

### Hardware Configuration Names

Motors: `leftFront`, `leftBack`, `rightBack`, `rightFront`, `intakeMotor`, `shooterMotorLeft`, `shooterMotorRight`, `liftMotor`
Servos: `tiltServo`, `kickerServo`, `carouselServo`
IMU: `imu`

### Important Patterns

- Classes annotated with `@Config` expose their `static public` fields to FTC Dashboard for live tuning
- Drive parameters (kS, kV, kA, PID gains, wheel velocity limits) live in `MecanumDrive.Params`
- The `MecanumDrive` constructor uses `LynxModule.BulkCachingMode.AUTO` for efficient hub communication
- `rightFront` motor direction is reversed in `MecanumDrive`; other motor directions are default
