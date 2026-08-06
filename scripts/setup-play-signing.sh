#!/usr/bin/env bash
set -euo pipefail

config_root="${XDG_CONFIG_HOME:-$HOME/.config}/aviator-watch"
keystore_path="$config_root/aviator-upload.jks"
certificate_path="$config_root/aviator-upload-certificate.pem"
key_alias="aviator-upload"
profile_path="$HOME/.bash_profile"

if [[ -e "$keystore_path" ]]; then
  echo "Refusing to replace existing upload keystore: $keystore_path" >&2
  exit 1
fi

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/keytool" ]]; then
  keytool_command="$JAVA_HOME/bin/keytool"
else
  keytool_command="$(command -v keytool)"
fi

mkdir -p "$config_root"
chmod 700 "$config_root"

echo "Creating the Google Play upload key. Choose a strong password and save it in a password manager."
read -rsp "Upload keystore/key password: " signing_password
echo
read -rsp "Confirm password: " signing_password_confirmation
echo
if [[ "$signing_password" != "$signing_password_confirmation" ]]; then
  echo "Passwords do not match." >&2
  exit 1
fi
if [[ ${#signing_password} -lt 12 ]]; then
  echo "Use a password containing at least 12 characters." >&2
  exit 1
fi

export AVIATOR_SIGNING_PASSWORD="$signing_password"
"$keytool_command" -genkeypair \
  -keystore "$keystore_path" \
  -storetype PKCS12 \
  -storepass:env AVIATOR_SIGNING_PASSWORD \
  -alias "$key_alias" \
  -keypass:env AVIATOR_SIGNING_PASSWORD \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000

chmod 600 "$keystore_path"
"$keytool_command" -exportcert -rfc \
  -keystore "$keystore_path" \
  -storepass:env AVIATOR_SIGNING_PASSWORD \
  -alias "$key_alias" \
  -file "$certificate_path"
chmod 600 "$certificate_path"

{
  echo
  echo "# Aviator Watch Google Play upload signing"
  printf 'export ANDROID_KEYSTORE_PATH=%q\n' "$keystore_path"
  printf 'export ANDROID_KEYSTORE_PASSWORD=%q\n' "$signing_password"
  printf 'export ANDROID_KEY_ALIAS=%q\n' "$key_alias"
  printf 'export ANDROID_KEY_PASSWORD=%q\n' "$signing_password"
} >> "$profile_path"
chmod 600 "$profile_path"
unset signing_password signing_password_confirmation AVIATOR_SIGNING_PASSWORD

echo
echo "Upload keystore: $keystore_path"
echo "Upload certificate: $certificate_path"
echo "Key alias: $key_alias"
echo "Signing environment added to: $profile_path"
echo
echo "Back up the keystore and password separately. Never commit either one."
