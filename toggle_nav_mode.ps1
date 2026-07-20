# This script toggles the Android Emulator's navigation mode between Gesture and 3-Button.

# Check if adb is available
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Error "ADB is not found in your system PATH. Please make sure Android SDK platform-tools is in your PATH."
    exit 1
}

# Get current navigation mode
$navMode = (adb shell settings get secure navigation_mode).Trim()

if ($navMode -eq "2") {
    Write-Host "Current mode: Gesture Navigation." -ForegroundColor Cyan
    Write-Host "Switching to 3-Button Navigation..." -ForegroundColor Yellow

    # Enable 3-button, disable gestural
    $null = adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton
    $null = adb shell cmd overlay disable com.android.internal.systemui.navbar.gestural
    $null = adb shell settings put secure navigation_mode 0

    Write-Host "Switched to 3-Button Navigation!" -ForegroundColor Green
} else {
    Write-Host "Current mode: 3-Button Navigation." -ForegroundColor Cyan
    Write-Host "Switching to Gesture Navigation..." -ForegroundColor Yellow

    # Enable gestural, disable 3-button
    $null = adb shell cmd overlay enable com.android.internal.systemui.navbar.gestural
    $null = adb shell cmd overlay disable com.android.internal.systemui.navbar.threebutton
    $null = adb shell settings put secure navigation_mode 2

    Write-Host "Switched to Gesture Navigation!" -ForegroundColor Green
}
