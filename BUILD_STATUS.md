# Actual build status — 2 November 2026

## Local Windows build: PASSED

Command: `gradle --no-daemon --console=plain assembleDebug testDebugUnitTest lintDebug`

- Result: **BUILD SUCCESSFUL in 2m 11s**, 49 tasks executed.
- Bundled `gradlew.bat` also verified successfully (1m 2s; 48 tasks cached),
  including downloading and checksum-verifying the pinned Gradle distribution.
- JDK: Eclipse Temurin 17.0.20.1; Gradle 8.13; Android Gradle Plugin 8.13.2.
- Compile/target API 36; min API 34. AGP selected Build Tools 35.0.0.
- Official Nothing Glyph Matrix SDK 2.0 pinned by commit and SHA-256.
- JUnit: **4 tests passed, 0 failures, 0 errors**. Covers long-running bounds under
  simulated shakes/delayed frames, flat settling, gravity/rebound, and 13×13 pixels.
- Lint: **0 errors, 4 warnings** (newer Gradle available, exported Toy service as
  required by Nothing's host, backup configuration, English button string).
- APK signature: **verified**, v2 signature, one debug signer.
- Package: `dev.gravityeyes`, version `0.1.0` (1).
- APK SHA-256: `c58f3475a4c66c99dc586a90ea2fbd5c0e5e56342c764dfb9931495ef44a931d`.

## Not yet verified

- Installation, SDK registration and output on a physical Phone (4a) Pro.
- Rear-view axis direction, exact rendered eye appearance and brightness.
- Animation rate, AOD minute events and accelerometer delivery while screen off.
- Host rebind/reconnect behavior on Nothing OS.

The SDK documentation supports this device and AOD service model. Successful compilation
is not proof of functioning rear-matrix animation. No hardware-tested claim is made.

## GitHub Actions

## GitHub Actions: PASSED

Run: https://github.com/hf-keo/glypheye/actions/runs/41390421146

- Tested commit: `7aa785ad30afdd002ea82506af0e488519180528`.
- Overall conclusion: **success**.
- Compilation, unit tests, lint and APK artifact upload all passed on Ubuntu.
- Download: https://github.com/hf-keo/glypheye/actions/runs/41390421146/artifacts/13117176646
- Artifact `GravityEyes-debug-APK` contains the debug APK and its checksum.
- `validation-reports` contains the CI test and lint reports.
- The first run failed before compilation because setup-android tried to install
  Google's removed legacy `tools` package. Workflow corrected to install only
  `platform-tools`, with platform/build tools installed in the following step.

CI uses its own temporary debug signing key. The local APK checksum above describes
the local artifact, not the independently signed CI APK. Neither build has been
installed or exercised on physical Nothing hardware.

