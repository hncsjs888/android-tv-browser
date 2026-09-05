$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$buildFile = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'app/build.gradle')
$workflow = Get-Content -Raw -Encoding UTF8 (Join-Path $root '.github/workflows/build.yml')
$activityPath = Join-Path $root 'app/src/legacy/java/com/ykq/tvbrowser/MainActivity.java'

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

Write-Host 'legacy GeckoView 构建契约检查通过'
