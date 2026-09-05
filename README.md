# 电视看板 Android TV

启动后全屏打开固定首页：

`http://212.64.0.247:5178/screen/hanging-output?lang=zh-CN`

## GitHub Actions 构建

请把本目录中的全部内容作为 GitHub 仓库根目录上传，不要再包一层 `android-tv-browser` 目录。

上传后打开仓库的 `Actions` 页面，选择 `Build Android TV APK`，点击 `Run workflow`，或直接向 `main` 分支提交一次代码。

构建完成后，在任务页面的 `Artifacts` 下载：

`tv-browser-armeabi-v7a-geckoview-debug`

APK 架构为 `armeabi-v7a`，适合 32 位 ARM 盒子。应用最低支持 Android 5.0（API 21）。该版本内置 GeckoView 128，不依赖盒子自带的旧 WebView。

## 海信高版本构建

在 Actions 中运行 `Build High Version Android TV APK`，下载 `tv-browser-arm64-v8a-high-debug`。

高版本 APK 使用 `arm64-v8a`，最低支持 Android 8.0（API 26），应用包名为 `com.ykq.tvbrowser.high`，可以和低版本 APK 共存。

该 APK 适用于 Android TV 或 Google TV 海信电视，不适用于 VIDAA、Roku 等非 Android 系统。
