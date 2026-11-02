#!/usr/bin/env bash
# Builds the debug APK and copies it to D:\android_projects\DevAgent.apk.
# Usage: ./build.sh            -> build only
#        ./build.sh install    -> build + install on a device connected via adb
set -euo pipefail
export JAVA_HOME="${JAVA_HOME:-$HOME/android-dev/jdk}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-dev/sdk}"
cd "$(dirname "$0")"
./gradlew assembleDebug --console=plain -q
cp app/build/outputs/apk/debug/app-debug.apk ../DevAgent.apk
echo "APK ready: D:\\android_projects\\DevAgent.apk"
if [[ "${1:-}" == "install" ]]; then
  "$ANDROID_HOME/platform-tools/adb" install -r app/build/outputs/apk/debug/app-debug.apk
fi
