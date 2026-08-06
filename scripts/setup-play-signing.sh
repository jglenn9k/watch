#!/usr/bin/env bash
set -euo pipefail

config_root="${XDG_CONFIG_HOME:-$HOME/.config}/aviator-watch"
keystore_path="$config_root/aviator-upload.jks"
certificate_path="$config_root/aviator-upload-certificate.pem"
key_alias="aviator-upload"

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
echo "Use the same password for the keystore and key when keytool asks."
"$keytool_command" -genkeypair \
  -keystore "$keystore_path" \
  -storetype PKCS12 \
  -alias "$key_alias" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000

chmod 600 "$keystore_path"
"$keytool_command" -exportcert -rfc \
  -keystore "$keystore_path" \
  -alias "$key_alias" \
  -file "$certificate_path"
chmod 600 "$certificate_path"

echo
echo "Upload keystore: $keystore_path"
echo "Upload certificate: $certificate_path"
echo "Key alias: $key_alias"
echo
echo "Back up the keystore and password separately. Never commit either one."
