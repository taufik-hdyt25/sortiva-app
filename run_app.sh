#!/usr/bin/env bash

# ==============================================================================
# Script: run_app.sh
# Description: Builds, installs, and runs Sortiva on an Android device or emulator
# ==============================================================================

set -e

# Project configuration
PACKAGE_NAME="com.sortiva"
ACTIVITY_NAME=".MainActivity"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$PROJECT_DIR"

# 1. Locate Android SDK & ADB
if [ -n "$ANDROID_HOME" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
    ADB="$ANDROID_HOME/platform-tools/adb"
elif [ -n "$ANDROID_SDK_ROOT" ] && [ -x "$ANDROID_SDK_ROOT/platform-tools/adb" ]; then
    ADB="$ANDROID_SDK_ROOT/platform-tools/adb"
elif [ -f "local.properties" ] && grep -q "sdk.dir" "local.properties"; then
    SDK_DIR=$(grep "sdk.dir" "local.properties" | cut -d'=' -f2 | tr -d ' \r')
    ADB="$SDK_DIR/platform-tools/adb"
elif [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB="$HOME/Library/Android/sdk/platform-tools/adb"
elif command -v adb >/dev/null 2>&1; then
    ADB=$(command -v adb)
else
    echo "❌ Error: Could not locate 'adb'. Please ensure Android SDK platform-tools is installed."
    exit 1
fi

echo "🔍 Using ADB at: $ADB"

# 2. Ensure JAVA_HOME points to a full JDK with jlink
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/jlink" ]; then
    if [ -x "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home/bin/jlink" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"
    elif [ -n "$(/usr/libexec/java_home 2>/dev/null)" ] && [ -x "$(/usr/libexec/java_home)/bin/jlink" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home)"
    fi
fi
if [ -n "$JAVA_HOME" ]; then
    echo "☕ Using JDK at: $JAVA_HOME"
fi

# 3. Check for connected devices
DEVICES_OUTPUT=$("$ADB" devices | grep -v "List of devices attached" | grep -v "^$" || true)

if [ -z "$DEVICES_OUTPUT" ]; then
    echo ""
    echo "⚠️  No Android device or emulator detected!"
    echo "👉 Please connect your phone via USB (with USB Debugging enabled)"
    echo "   or start an emulator from Android Studio / CLI, then run this script again."
    exit 1
fi

if echo "$DEVICES_OUTPUT" | grep -q "unauthorized"; then
    echo ""
    echo "⚠️  Device detected but UNAUTHORIZED!"
    echo "👉 Please check your phone screen and tap 'Allow' for USB Debugging."
    exit 1
fi

echo "📱 Connected device(s):"
echo "$DEVICES_OUTPUT"
echo ""

# 3. Build and install debug APK
echo "🚀 Building and installing debug APK via Gradle..."
./gradlew installDebug -Dorg.gradle.java.home="$JAVA_HOME"

# 4. Launch or restart the app
echo "✨ Restarting $PACKAGE_NAME$ACTIVITY_NAME with updated code..."
"$ADB" shell am start -S -n "$PACKAGE_NAME/$ACTIVITY_NAME"

echo ""
echo "✅ App started successfully!"

# 5. Optional logcat streaming
if [ "$1" == "--logs" ] || [ "$1" == "-l" ]; then
    echo "📋 Streaming logs (Ctrl+C to stop)..."
    "$ADB" logcat --pid="$("$ADB" shell pidof -s "$PACKAGE_NAME" 2>/dev/null || true)"
else
    echo "💡 Tip: Run './run_app.sh --logs' to stream app logs, or run:"
    echo "   $ADB logcat -s MainActivity"
fi
