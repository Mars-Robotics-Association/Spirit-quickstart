# AGENTS.md - Team Spirit FTC Robotics

## Project Overview

This is an FTC (FIRST Tech Challenge) robotics project for Team Spirit, built on Road Runner v1.0. The project is an Android application that deploys to an FTC Robot Controller REV Control Hub. Team code lives in the `TeamCode` module.

## Build Commands

All commands use Gradle with the Android Gradle Plugin. Run from the project root.

```bash
# Build entire project
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Clean and rebuild
./gradlew clean assembleDebug
```

There are no unit tests in this project. Testing is done by deploying to the robot and running OpModes via the FTC Driver Station.

## Code Style Guidelines

### File Organization

- Package declaration: `org.firstinspires.ftc.teamcode`
- Subpackages: `robot`, `opmodes.teleop`, `opmodes.auto`, `opmodes.tuning`, `utils`, `messages`, `tuning`
- One public class per file
- Filename matches class name

### Imports

Order imports by:

1. Android/SDK imports
2. Road Runner / Acmerobotics imports
3. Qualcomm (robotcore) imports
4. Static imports
5. Team internal imports

```java
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.Shooter;
```

### Naming Conventions

- **Classes:** PascalCase (`MecanumDrive`, `SpiritTeleop2`)
- **Methods:** camelCase (`updatePoseEstimate`, `setDrivePowers`)
- **Constants:** SCREAMING_SNAKE_CASE with descriptive names (`nearTiltPosition`, `farShooterVelocity`)
- **Instance fields:** camelCase with `m` prefix optional; prefer descriptive names (`left`, `right`, `telemetry`)
- **Parameters:** camelCase (`hardwareMap`, `telemetry`)
- **Config fields (tunable):** `static public` with descriptive names

### Formatting

- 4-space indentation (no tabs)
- Line length: 100-120 characters max
- One statement per line
- Opening brace on same line
- Use blank lines to separate logical sections within methods

### Annotations

- `@Config` - Exposes static fields to FTC Dashboard for live tuning
- `@TeleOp` / `@Autonomous` - OpMode registration
- `@Override` - Always use when overriding methods

### FTC SDK Patterns

#### DcMotor RunMode

- `RUN_USING_ENCODER` - Enables SDK's built-in PIDF velocity controller
- `RUN_WITHOUT_ENCODER` - Correct mode for custom feedback/feedforward; still reads encoders

#### Feedforward (kV) Calculation

The SDK's PIDF `F` coefficient is kV scaled by 32767:

```java
F = 32767 * kV  // e.g., max 2496 ticks/sec → F = 32767 / 2496 ≈ 13.13
```

#### Gamepad Edge Detection

Use SDK 11.0+ built-in methods:

```java
gamepad1.leftBumperWasPressed()
gamepad1.rightBumperWasReleased()
gamepad1.xWasPressed()
```

#### Hub Detection

Use `HubHelper` to determine which hub a motor is on - don't hardcode hub assumptions.

### Class Structure

#### Subsystems (robot package)

- Constructor takes `HardwareMap` and optionally `Telemetry`
- `static public` fields for tunable parameters (with `@Config`)
- Update methods called each loop iteration
- Clear, descriptive Javadoc on public methods

#### OpModes

- Extend `LinearOpMode` for teleop/auto
- Initialize hardware in `runOpMode()` before `waitForStart()`
- Main loop uses `while (opModeIsActive())`
- Usually nothing goes after the main loop, but especially not hardware commands
- Use gamepad edge detection for button state changes

#### Hardware Access

```java
// Motors
DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "motorName");
motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

// Servos
Servo servo = hardwareMap.get(Servo.class, "servoName");
servo.setPosition(0.5);

// Sensors
ColorSensor color = hardwareMap.get(ColorSensor.class, "color");
```

### Error Handling

- Avoid exceptions in control loops; use null checks instead
- Log errors via telemetry: `telemetry.addData("Error", message)`
- Don't catch exceptions silently - at minimum log and display
- Hardware initialization failures should stop the OpMode

### Telemetry & Debugging

- Use FTC Dashboard (`FtcDashboard.getInstance()`) for real-time tuning
- When using field overlay, use `DashboardTelemetryPacketAccess` to draw directly
- Don't create separate TelemetryPacket when using Dashboard telemetry
- Keep debug telemetry minimal during competition matches
- Use the format string parameter for `telemetry.addData` instead of `String.format`:
  ```java
  telemetry.addData("Velocity", "%.1f", value);  // correct
  telemetry.addData("Velocity", String.format("%.1f", value));  // avoid
  ```

### Hardware Shutdown

**Critical:** Never send hardware commands after `opModeIsActive()` returns false. This crashes the Control Hub.

```java
@Override
public void runOpMode() {
    DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "motorName");
    motor.setPower(0)
    waitForStart();
    while (opModeIsActive()) {
        // only here
    }
    // never here
}
```

### Progress Reports

When requested, create progress reports in:
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ProgressReports/`

Include: problems discussed, decisions made, gotchas encountered, solutions reached.
