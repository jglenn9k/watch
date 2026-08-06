# Aviator Digital for Galaxy Watch5 Pro

A high-contrast Wear OS watch face for general-aviation pilots. Local time is
primary, UTC is always shown in 24-hour format, and the four lower panels show
magnetic heading, raw ambient pressure, blood oxygen, and weather.

> This project is not certified flight equipment. Pressure is the watch sensor's
> local ambient pressure—not QNH or an airport altimeter setting. SpO2 is fitness
> and wellness information only and must not be used for diagnosis, treatment, or
> flight-safety decisions.

## Project layout

- `watchface`: code-free Watch Face Format app (`com.jglenn.aviator.watchface`).
- `sensor-provider`: Wear OS app (`com.jglenn.aviator.sensors`) with heading,
  pressure, and SpO2 complication data sources.

Wear OS requires these to be separate application bundles. The face defaults its
three sensor slots to the production provider package. Weather remains a normal
user-selectable complication, so no location permission, weather API, or API key
is needed.

The face deliberately declares WFF version 1 (Wear OS 4 / API 33) because every
feature used here is available in that schema and it preserves compatibility with
the Watch5 Pro. This is the current declarative WFF packaging model, without
unnecessarily requiring Wear OS 6.

## Requirements

- Android Studio with Android 16 / API 36 SDK and OpenJDK 25.0.4 (2026-07-21 LTS)
- Galaxy Watch5 Pro SM-R920 (physical hardware is required for Samsung SpO2)
- Watch and workstation on the same Wi-Fi network for wireless ADB
- Samsung Health Sensor SDK 1.4.1 and current Health Platform for real SpO2

This repository intentionally does not include the proprietary Samsung AAR or a
signing key.

## Build

Open the repository in Android Studio and let it install the requested Gradle and
SDK components. Two provider variants are available:

```text
demoDebug       Builds without Samsung's AAR; SpO2 explains how to enable it.
samsungDebug    Uses the real Samsung Health Sensor SDK.
```

To enable real SpO2:

1. Sign in to Samsung Developer and download Samsung Health Sensor SDK v1.4.1.
2. Extract the download and copy its `samsung-health-sensor-api.aar` to
   `sensor-provider/libs/samsung-health-sensor-api.aar`.
3. Build `:sensor-provider:assembleSamsungDebug` and
   `:watchface:assembleDebug`.

For Play-ready bundles, configure release signing in your private Gradle settings,
then build `:sensor-provider:bundleSamsungRelease` and
`:watchface:bundleRelease`. They are separate Play artifacts and listings.

## Google Play signing

Use Google Play App Signing and let Google generate the app-signing key for each
new Play listing. Keep one local upload key for signing both AAB uploads. Create it
interactively; passwords are never written into this repository:

```bash
scripts/setup-play-signing.sh
```

This creates the private upload key and public upload certificate under
`~/.config/aviator-watch/`, then adds the four `ANDROID_*` signing environment
variables to `~/.bash_profile` with owner-only permissions. Back up the `.jks`
file and its password separately. The profile contains the passwords in plaintext,
so never publish or copy it into the repository.
After installing the Samsung AAR, produce signed local Play artifacts with a new,
monotonically increasing version code:

```bash
scripts/build-play-release.sh 1.0.0 1
```

Create two Play Console apps, one for `com.jglenn.aviator.watchface` and one for
`com.jglenn.aviator.sensors`, and upload the corresponding AAB to an internal-test
release. Accept Play App Signing with Google's generated key for each app.

For the sensor provider, copy the **App signing key certificate SHA-256** from its
Play Console App integrity page and submit that fingerprint, package name
`com.jglenn.aviator.sensors`, and the SpO2 use case in Samsung's partnership
request. Do not submit the upload-key fingerprint: Google re-signs delivered APKs
with the app-signing key. Until Samsung approves the production signature, use
Health Platform Developer Mode only for local testing.

## Install on the SM-R920

Enable Developer mode, ADB debugging, and Wireless debugging on the watch. Pair
ADB, then install both production-package APKs:

```bash
adb connect WATCH_IP:ADB_PORT
adb install -r sensor-provider/build/outputs/apk/samsung/debug/sensor-provider-samsung-debug.apk
adb install -r watchface/build/outputs/apk/debug/watchface-debug.apk
```

Select **Aviator Digital** in the watch-face picker. If a default sensor provider
did not bind, edit the face and assign:

- Magnetic heading → **Aviator magnetic heading**
- Ambient pressure → **Aviator ambient pressure**
- Blood oxygen → **Aviator blood oxygen**
- Weather → Samsung Weather, Google Weather, or another installed provider

The demo and Samsung flavors intentionally use the same application ID. Install
only one provider flavor at a time; a Samsung build can upgrade a demo build when
both are signed with the same key.

## Sensor behavior

- **HDG:** the face shows the last heading captured by the live compass. Tap to
  open it, hold the watch face-up, and perform a figure-eight motion if prompted.
  The result is magnetic—not true—heading. `*` indicates a reading older than 15
  minutes.
- **BARO:** sampled on provider requests and from the sensor dashboard. It uses
  `inHg = hPa × 0.0295299830714` and updates at most every 30 minutes under Wear
  OS complication scheduling.
- **SpO2:** tap to grant the foreground health permission and begin an on-demand,
  up-to-30-second measurement. `*` indicates a reading older than 24 hours.
- **WX:** data and tap behavior come entirely from the provider selected in the
  watch-face editor.

Samsung SDK testing may require enabling Developer mode in the watch's Health
Platform app. A public release also requires registering the production package,
signing-certificate SHA-256, and SpO2 tracker scope with Samsung; otherwise the SDK
can report `SDK_POLICY_ERROR`.

## Verification

Run unit tests with `:sensor-provider:testDemoDebugUnitTest`. Validate
`watchface/src/main/res/raw/watchface.xml` with Android Studio's WFF validator and
memory-footprint tool before publishing. Test compass accuracy, barometer presence,
permission denial, off-wrist/low-signal/movement/timeout SpO2 states, ambient burn-in,
timezone/DST transitions, and all tap targets on the physical watch.

## GitHub binary releases

The included GitHub Actions workflow runs tests, builds signed APK/AAB artifacts,
writes SHA-256 checksums, and creates a GitHub Release for tags matching `v*`.

Configure these repository secrets under **Settings → Secrets and variables →
Actions** before tagging:

- `ANDROID_KEYSTORE_BASE64`: base64-encoded release keystore
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `SAMSUNG_HEALTH_SENSOR_AAR_BASE64`: optional but required for a real SpO2 release

Without the Samsung secret, the release contains the demo provider and names it
accordingly. Without all four signing secrets, the release build fails rather than
publishing unsigned APKs.

Create and push a semantic version tag:

```bash
git tag -a v1.0.0 -m "Aviator Digital v1.0.0"
git push origin v1.0.0
```

The release assets are also retained as workflow artifacts for 30 days.
