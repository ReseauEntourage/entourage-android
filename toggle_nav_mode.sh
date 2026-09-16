#!/bin/sh

# This script toggles the Android Emulator's navigation mode between Gesture and 3-Button on Linux/macOS.

# Check if adb is available
if ! command -v adb >/dev/null 2>&1; then
    echo "\033[0;31mError: ADB is not found in your system PATH. Please make sure Android SDK platform-tools is in your PATH.\033[0m"
    exit 1
fi

# Get current navigation mode and remove trailing carriage returns
navMode=$(adb shell settings get secure navigation_mode | tr -d '\r\n[:space:]')

if [ "$navMode" = "2" ]; then
    echo "\033[0;36mCurrent mode: Gesture Navigation.\033[0m"
    echo "\033[0;33mSwitching to 3-Button Navigation...\033[0m"

    # Enable 3-button, disable gestural
    adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton >/dev/null
    adb shell cmd overlay disable com.android.internal.systemui.navbar.gestural >/dev/null
    adb shell settings put secure navigation_mode 0 >/dev/null

    echo "\033[0;32mSwitched to 3-Button Navigation!\033[0m"
else
    echo "\033[0;36mCurrent mode: 3-Button Navigation.\033[0m"
    echo "\033[0;33mSwitching to Gesture Navigation...\033[0m"

    # Enable gestural, disable 3-button
    adb shell cmd overlay enable com.android.internal.systemui.navbar.gestural >/dev/null
    adb shell cmd overlay disable com.android.internal.systemui.navbar.threebutton >/dev/null
    adb shell settings put secure navigation_mode 2 >/dev/null

    echo "\033[0;32mSwitched to Gesture Navigation!\033[0m"
fi
