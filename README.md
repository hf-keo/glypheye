# Gravity Eyes

Experimental native AOD Glyph Toy for Nothing Phone (4a) Pro (13 × 13).
Two dark pupils move inside white eyes using filtered accelerometer gravity,
independent damped springs and collision restitution. Runs at a requested 30 fps
while Nothing's host binds the toy; unregisters sensors and releases the SDK on unbind.
No ads, network permission, tracking, or foreground service.

## Install and select

Install `GravityEyes-debug.apk` (allow installation from the app you downloaded it with).
Then choose **Settings → Glyph Interface → Flip to Glyph → Always-on Glyph Toy → Gravity Eyes**.
The launcher also links to Nothing's Toy manager. This model has no Glyph Touch.
Test tilts in both axes, flip face down, screen off, deselection, and reselecting.
Rear-axis direction and continuous screen-off sensor delivery require real-device validation.
Android can restrict continuous accelerometer delivery to background apps; this bound
toy follows Nothing's documented model and does not promise uninterrupted AOD motion.

## Build locally

Requirements: JDK 17, Android SDK platform 36 / Build Tools 36.0.0, Python 3.
Set ANDROID_HOME or a local.properties sdk.dir. Run:

```
python scripts/fetch_sdk.py
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Windows: use `gradlew.bat`. APK: `app/build/outputs/apk/debug/app-debug.apk`.
Debug APKs use a development signing key, not a stable release identity. A build from
a different machine/CI run can require uninstalling the prior build before installation.

## GitHub Actions

The included workflow runs on main pushes, pull requests, or **Actions → Build Gravity Eyes APK → Run workflow**.
Open the successful run and download **GravityEyes-debug-APK** from Artifacts (GitHub sign-in required).
Extract the ZIP to obtain the APK and SHA-256. Validation reports upload even when checks fail.
No APK is uploaded if compilation, tests, or lint fail.

## Official SDK verification

SDK 2.0 from Nothing-Developer-Programme/GlyphMatrix-Developer-Kit, pinned to
`999b1143a6bff0dd79db11d21b92c15c56a346b1`.
SHA-256: `be00ee9cd7115f6b11984c6e31fe98e298fb726940d1555063610685ef3bbf29`.
Downloaded during builds rather than committed. Its own license applies:
https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit/blob/999b1143a6bff0dd79db11d21b92c15c56a346b1/LICENSE.md

Public signatures checked against this AAR: `register(String)` returns boolean;
`GlyphMatrixFrame.Builder.build(Context)` requires context; `setMatrixFrame(frame)`
throws GlyphException. Uses `Glyph.DEVICE_25111p` and `aod_support=1`, receives
`EVENT_AOD` through a Messenger. Structured frames let the SDK select matrix dimensions.

Docs: https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit

Compilation alone does not verify real hardware behavior. See BUILD_STATUS.md for actual checks.
