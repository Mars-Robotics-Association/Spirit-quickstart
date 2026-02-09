## Project Overview

This is an FTC (FIRST Tech Challenge) robotics project for Team Spirit, built on the Road Runner v1.0 quickstart. It is an Android application that deploys to an FTC Robot Controller REV Control Hub. The robot features a mecanum drivetrain with a ball shooter mechanism (carousel, kicker, tilt servo), intake motor, and lift. The team's code starts after `08f0898`

## Build Commands

This project uses Gradle with the Android Gradle Plugin. All commands should be run from the project root. Use `./gradlew` directly from bash — do NOT use `gradlew.bat` or `cmd /c`. For git commands, avoid using `git -C <path>` — just run `git` from the project root instead.

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

### Important Patterns

- Classes annotated with `@Config` expose their `static public` fields to FTC Dashboard for live tuning
- Drive parameters (kS, kV, kA, PID gains, wheel velocity limits) live in `MecanumDrive.Params`
- The `MecanumDrive` constructor uses `LynxModule.BulkCachingMode.AUTO` for efficient hub communication

## FTC SDK Gotchas

### DcMotor RunMode

- `RUN_USING_ENCODER` enables the SDK's built-in PIDF velocity controller. Only use this if you want the SDK to handle velocity control.
- `RUN_WITHOUT_ENCODER` is the correct mode when doing custom feedback/feedforward in team code. Despite the name, it still reads encoder values — it just doesn't use them for internal control.

### RUN_USING_ENCODER feedforward (kV)

The SDK's velocity PIDF `F` coefficient is a kV, but scaled by 32767. To calculate: `F = 32767 * kV`. For example, a motor measured at max 2496 ticks/sec → `F = 32767 / 2496 ≈ 13.13`.

### Gamepad edge detection

SDK 11.0 added rising/falling edge detection methods directly on the Gamepad object, e.g. `gamepad1.leftBumperWasPressed()` and `gamepad1.leftBumperWasReleased()`. SDK 11.1 extended this to triggers. See `ConceptGamepadEdgeDetection` sample for usage. Code targeting older SDK versions must track previous button state manually.

### FTC Dashboard field overlay

When using FTC Dashboard telemetry alongside field overlay drawing (e.g., in `DrivingBase`), use `DashboardTelemetryPacketAccess` to get the underlying `TelemetryPacket` and draw on its `fieldOverlay()` directly. Do not create a separate `TelemetryPacket` and send it manually — that writes extra telemetry lines and duplicates data on the dashboard.

