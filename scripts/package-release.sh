#!/usr/bin/env bash
set -euo pipefail

release_dir="${1:-release}"

mkdir -p "$release_dir"

cp watchface/build/outputs/apk/release/watchface-release.apk "$release_dir/aviator-watchface.apk"
cp watchface/build/outputs/bundle/release/watchface-release.aab "$release_dir/aviator-watchface.aab"
cp sensor-provider/build/outputs/apk/release/sensor-provider-release.apk "$release_dir/aviator-sensors.apk"
cp sensor-provider/build/outputs/bundle/release/sensor-provider-release.aab "$release_dir/aviator-sensors.aab"

(
  cd "$release_dir"
  sha256sum ./*.apk ./*.aab > SHA256SUMS
  tar -czf "aviator-watch-${RELEASE_VERSION_NAME:-dev}.tar.gz" ./*.apk ./*.aab SHA256SUMS
)
