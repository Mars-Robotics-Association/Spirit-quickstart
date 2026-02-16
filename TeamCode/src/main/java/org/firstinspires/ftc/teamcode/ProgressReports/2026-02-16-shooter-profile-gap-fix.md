# Progress Report — 2026-02-16

## Problem

After the first shot in SpiritTeleop2, the shooter velocity profile goes wonky on the second shot. The root cause: `Shooter.update()` is not called every control loop iteration. The teleop's state machine only calls `setShooterVelocity` (which delegates to `update()`) inside specific state/step branches. There are multi-second gaps — especially during IDLE between launch sequences, and during certain LAUNCHING steps (step 0, steps 13-16).

When `update()` finally runs after a gap, `dt` (computed from `System.nanoTime()`) has accumulated to seconds. The profile step `profiledVelocity += accel * dt` then massively overshoots the target. For example, with a 5-second gap targeting 650 tps: `accel ≈ 435 tps²`, so `profiledVelocity += 435 * 5.0 = 2175` — 3.3x the target.

## Constraint

The user wanted to avoid modifying the Spirit opmodes. The correct fix would be to call `update()` every loop iteration in SpiritTeleop2, but instead we made Shooter defensive against gaps.

## Solution

Two changes, both in the Shooter/ShooterMotor subsystem:

### 1. Profile dt cap (`Shooter.java`)

Added `public static double maxProfileDt = 0.100` (tunable via Dashboard). The profile step now uses `Math.min(dt, maxProfileDt)` instead of raw `dt`. The velocity filter still uses the true `dt` so its IIR alpha is computed correctly.

This keeps the profile close to the motor's actual trajectory after a gap, which also keeps the kA feedforward term accurate — the profile's acceleration matches what the motor physically needs.

Telemetry line `"profile dt CAPPED"` is emitted when the cap activates, for debugging on Dashboard.

### 2. Filter reset on resume from coast (`ShooterMotor.java` + `Shooter.java`)

Added `ShooterMotor.resetFilter()` which snaps `smoothActualVelocity` to the latest raw encoder reading, discarding stale filter history. Called in Shooter's `stopped` -> running transition, before seeding the profile.

Without this, `smoothActualVelocity` could be stale from seconds ago (the last time `update()` ran). The IIR filter's alpha would be ~1.0 for a large dt (approximately correct), but relying on that is fragile. Explicit reset makes the intent clear and guarantees a clean start.

## Discussion: analytical profile vs dt cap

We explored whether the Euler integration could be replaced with the exact analytical solution for the clamped-exponential profile:

- **Exponential regime** (`|error| <= maxAccel * tau`): `v(t) = target - error * exp(-t/tau)` — never overshoots for any dt.
- **Clamped regime**: linear ramp, Euler is exact.
- **Split step** (crosses from clamped to exponential within one dt): computable exactly.

This eliminates numerical instability entirely. However, for this application, the dt cap is actually superior:

- With the exact solution and a 5s gap, the profile jumps to ~627 tps (near target) on the first step. But the motor is at 0 tps. The kA feedforward term sees near-zero acceleration and provides almost no voltage — exactly when the motor needs a strong kick to start moving.
- With the dt cap, the profile starts at ~150 tps with `accel = maxAccel`, so `kA * maxAccel` provides the large voltage the motor needs. The profile tracks close to the motor's actual trajectory.

**Decision:** Keep the dt cap. The profile's purpose is to give the motor a setpoint it can actually follow, not to solve the ODE correctly. When there's been a control gap, "resume from where the motor is and ramp normally" is the right behavior.

## Files Changed

- `TeamCode/.../robot/Shooter.java` — added `maxProfileDt` field, capped profile dt, added filter reset call on resume, added telemetry for capped dt
- `TeamCode/.../robot/ShooterMotor.java` — added `resetFilter()` method
