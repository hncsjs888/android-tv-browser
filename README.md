# 电视看板 Android TV

启动后全屏打开固定首页：

`http://212.64.0.247:5178/screen/hanging-output?lang=zh-CN`

## GitHub Actions 构建

请把本目录中的全部内容作为 GitHub 仓库根目录上传，不要再包一层 `android-tv-browser` 目录。

上传后打开仓库的 `Actions` 页面，选择 `Build Android TV APK`，点击 `Run workflow`，或直接向 `main` 分支提交一次代码。

构建完成后，在任务页面的 `Artifacts` 下载：

`tv-browser-armeabi-v7a-debug`

APK 架构为 `armeabi-v7a`，适合 32 位 ARM 盒子。应用最低支持 Android 5.0（API 21）。
