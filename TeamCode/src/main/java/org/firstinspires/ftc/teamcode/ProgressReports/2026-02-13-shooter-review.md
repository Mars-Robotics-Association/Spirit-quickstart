# Progress Report — 2026-02-13

## Session Goal

Review recent changes to the Shooter subsystem (Shooter.java and ShooterMotor.java) covering the voltage-based feedforward rewrite, ShooterMotor extraction, and acceleration profiling.

## Bugs Found and Fixed

### Copy-paste error: `leftKA` passed to right motor

In `Shooter.update()`, the right motor's `update()` call was receiving `leftKA` instead of `rightKA` for the acceleration feedforward gain. Since the two motors' kA values differ by ~30% (`12.5/2380.5` vs `12.5/1835.8`), the right motor's acceleration compensation was consistently wrong.

**Fix:** Changed to `rightKA`.

### Uninitialized `lastTimeNanos` causing massive first `dt`

`lastTimeNanos` defaulted to 0. On the first call to `update()` with a nonzero target, `dt` would equal `System.nanoTime() / 1e9` (seconds since boot — potentially thousands of seconds), causing `profiledVelocity` to massively overshoot the target on the first iteration. The same problem recurred any time `shooterVelocity` was set to 0 and back, because the early return skipped updating `lastTimeNanos`.

**Fix:** Added a first-call guard: when `lastTimeNanos == 0`, seed it with the current time and return without running the control loop.

## Design Changes

### Velocity filter runs continuously (even when shooter is off)

The user wanted the smoothed velocity filters to stay current while the motors coast, so that on resume the profile can seed from the actual coasting speed rather than ramping from zero.

**Approach:** Split `ShooterMotor.update()` into two methods:
- `updateFilter(double alpha)` — reads the encoder and updates the IIR low-pass filter. Called every loop unconditionally.
- `update(...)` — runs feedforward + feedback control using the already-smoothed value. Only called when the shooter is active.

In `Shooter.update()`, timing and filter updates now run before the `shooterVelocity == 0` check. When velocity goes to zero, the motors coast but the filters keep tracking. On resume, `profiledVelocity` is seeded from `Math.max(left.smoothActualVelocity, right.smoothActualVelocity)` so the acceleration profile picks up from the faster motor's actual speed.

### Eliminated redundant `acceleration` / `prevProfiledVelocity`

The user noticed two acceleration variables: `accel` (clamped profile acceleration) and `acceleration` (derived from the delta of `profiledVelocity` and `prevProfiledVelocity`). Outside of the snap-to-target case, these are algebraically identical. During snap, the desired value is 0. Simplified to just use `accel` directly (set to 0 on snap), which also eliminated the `prevProfiledVelocity` field entirely.

### Added `isReady()` method

Added a method to indicate the shooter is up to speed and safe to fire. Returns true when:
1. `shooterVelocity != 0` (target is set)
2. `profiledVelocity == shooterVelocity` (profile has snapped to target, accel = 0)
3. Both motors' smoothed velocities are within `readyThreshold` (default 25 tps) of the target

`readyThreshold` is a `@Config` static field, tunable via FTC Dashboard.

## Minor Cleanup

- Removed unused `voltageSensor` field from Shooter (voltage is now read once in the constructor as a local)
- Removed unused `VoltageSensor` import from ShooterMotor
