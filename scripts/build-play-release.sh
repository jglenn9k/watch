#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 VERSION_NAME VERSION_CODE" >&2
  echo "Example: $0 1.0.0 1" >&2
  exit 2
fi

version_name="$1"
version_code="$2"
config_root="${XDG_CONFIG_HOME:-$HOME/.config}/aviator-watch"
keystore_path="${ANDROID_KEYSTORE_PATH:-$config_root/aviator-upload.jks}"
key_alias="${ANDROID_KEY_ALIAS:-aviator-upload}"
aar_path="sensor-provider/libs/samsung-health-sensor-api.aar"
release_dir="release/play-$version_name"

[[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z.-]+)?$ ]] || {
  echo "VERSION_NAME must resemble 1.0.0" >&2
  exit 2
}
[[ "$version_code" =~ ^[1-9][0-9]*$ ]] || {
  echo "VERSION_CODE must be a positive integer" >&2
  exit 2
}
[[ -f "$keystore_path" ]] || {
  echo "Upload keystore not found: $keystore_path" >&2
  echo "Run scripts/setup-play-signing.sh first." >&2
  exit 1
}
[[ -f "$aar_path" ]] || {
  echo "Samsung SDK AAR not found: $aar_path" >&2
  echo "Download Samsung Health Sensor SDK v1.4.1 and copy its AAR to that path." >&2
  exit 1
}
unzip -tq "$aar_path" >/dev/null || {
  echo "Samsung SDK file is not a valid AAR: $aar_path" >&2
  exit 1
}

if [[ -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]]; then
  read -rsp "Upload keystore password: " ANDROID_KEYSTORE_PASSWORD
  echo
fi
if [[ -z "${ANDROID_KEY_PASSWORD:-}" ]]; then
  read -rsp "Upload key password (usually the same): " ANDROID_KEY_PASSWORD
  echo
fi

export ANDROID_KEYSTORE_PATH="$keystore_path"
export ANDROID_KEYSTORE_PASSWORD
export ANDROID_KEY_ALIAS="$key_alias"
export ANDROID_KEY_PASSWORD
export RELEASE_VERSION_NAME="$version_name"
export RELEASE_VERSION_CODE="$version_code"

./gradlew --no-daemon --console=plain \
  :sensor-provider:testDemoDebugUnitTest \
  :watchface:assembleRelease \
  :watchface:bundleRelease \
  :sensor-provider:assembleSamsungRelease \
  :sensor-provider:bundleSamsungRelease

scripts/package-release.sh samsung "$release_dir"

echo
echo "Play bundles:"
echo "  $release_dir/aviator-watchface.aab"
echo "  $release_dir/aviator-sensors-samsung.aab"
echo "GitHub/sideload APKs are in the same directory."
