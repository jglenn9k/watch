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
  :watchface:verifyWatch5ProCompatibility \
  :sensor-provider:testDebugUnitTest \
  :watchface:assembleRelease \
  :watchface:bundleRelease \
  :sensor-provider:assembleRelease \
  :sensor-provider:bundleRelease

scripts/package-release.sh "$release_dir"

echo
echo "Play bundles:"
echo "  $release_dir/aviator-watchface.aab"
echo "  $release_dir/aviator-sensors.aab"
echo "GitHub/sideload APKs are in the same directory."
echo
echo "Google Play deployment (required for watch eligibility):"
echo "  1. In each app, enable Wear OS under Advanced settings > Form factors."
echo "  2. Opt in to Wear OS distribution and accept its terms."
echo "  3. Select 'Wear OS only' in the release form-factor selector."
echo "  4. Upload the matching AAB to a Wear OS test or production track."
echo "  5. Confirm SM-R920 is Supported in Reach and devices > Device catalog."
