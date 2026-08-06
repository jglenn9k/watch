#!/usr/bin/env bash
set -euo pipefail

variant="${1:-demo}"
release_dir="${2:-release}"

case "$variant" in
  demo|samsung) ;;
  *) echo "Expected provider variant: demo or samsung" >&2; exit 2 ;;
esac

mkdir -p "$release_dir"

cp watchface/build/outputs/apk/release/watchface-release.apk "$release_dir/aviator-watchface.apk"
cp watchface/build/outputs/bundle/release/watchface-release.aab "$release_dir/aviator-watchface.aab"
cp "sensor-provider/build/outputs/apk/${variant}/release/sensor-provider-${variant}-release.apk" "$release_dir/aviator-sensors-${variant}.apk"
cp "sensor-provider/build/outputs/bundle/${variant}Release/sensor-provider-${variant}-release.aab" "$release_dir/aviator-sensors-${variant}.aab"

(
  cd "$release_dir"
  sha256sum ./*.apk ./*.aab > SHA256SUMS
  tar -czf "aviator-watch-${RELEASE_VERSION_NAME:-dev}.tar.gz" ./*.apk ./*.aab SHA256SUMS
)

