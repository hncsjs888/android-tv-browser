$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$buildFile = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'app/build.gradle')
$workflow = Get-Content -Raw -Encoding UTF8 (Join-Path $root '.github/workflows/build.yml')
$activityPath = Join-Path $root 'app/src/legacy/java/com/ykq/tvbrowser/MainActivity.java'
$manifestPath = Join-Path $root 'app/src/main/AndroidManifest.xml'
$bootReceiverPath = Join-Path $root 'app/src/main/java/com/ykq/tvbrowser/BootReceiver.java'

if ($buildFile -notmatch "legacyImplementation\s+'org\.mozilla\.geckoview:geckoview-armeabi-v7a:") {
    throw 'legacy 构建尚未声明 GeckoView armeabi-v7a 依赖'
}

if ($workflow -notmatch ':app:assembleLegacyDebug') {
    throw '低版本工作流尚未构建 LegacyDebug'
}

if (-not (Test-Path $activityPath)) {
    throw 'legacy GeckoView Activity 不存在'
}

$activity = Get-Content -Raw -Encoding UTF8 $activityPath
if ($activity -notmatch 'GeckoView' -or $activity -notmatch 'GeckoSession') {
    throw 'legacy Activity 尚未使用 GeckoView'
}

if ($activity -notmatch 'KEYCODE_DPAD_RIGHT' -or $activity -notmatch 'KEYCODE_MENU') {
    throw 'legacy Activity 尚未配置遥控器退出和设置入口'
}

if ($activity -notmatch 'AlertDialog' -or $activity -notmatch 'AUTO_START') {
    throw 'legacy Activity 尚未配置退出确认或自启设置'
}

if ($activity -notmatch 'glMsaaLevel\(0\)' -or $activity -notmatch 'consoleOutput\(false\)') {
    throw 'legacy GeckoView 尚未启用低配渲染设置'
}

$manifest = Get-Content -Raw -Encoding UTF8 $manifestPath
if ($manifest -notmatch 'RECEIVE_BOOT_COMPLETED' -or $manifest -notmatch 'BootReceiver') {
    throw '尚未声明开机自启权限或接收器'
}

if (-not (Test-Path $bootReceiverPath)) {
    throw '开机自启接收器不存在'
}

Write-Host 'legacy GeckoView 构建契约检查通过'
