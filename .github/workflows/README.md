# GitHub Actions Workflows 配置说明

## 已创建的 Workflows

### 1. Android CI (`android-build.yml`)
**触发条件：**
- 推送到 `main` 或 `develop` 分支
- 向 `main` 分支创建 Pull Request
- 手动触发

**执行任务：**
- 构建项目 (`./gradlew build`)
- 运行测试 (`./gradlew test`)
- 代码检查 (`./gradlew lint`)
- 上传构建产物和检查报告

### 2. Android Release (`android-release.yml`)
**触发条件：**
- 推送版本标签 (如 `v1.0.0`)
- 手动触发

**执行任务：**
- 构建 Release APK 和 AAB
- 使用签名密钥签名
- 创建 GitHub Release 并上传构建产物

## 需要在 GitHub 仓库中配置的 Secrets

### 对于 Release 构建（可选）
如果你需要构建签名的 APK/AAB，需要在 GitHub 仓库的 Settings → Secrets and variables → Actions 中添加以下 secrets：

1. **ANDROID_KEYSTORE_BASE64**
   - 描述：Base64 编码的 keystore 文件
   - 生成命令：`base64 keystore.jks`
   - 注意：需要先有本地的 keystore 文件

2. **ANDROID_KEYSTORE_PASSWORD**
   - 描述：Keystore 密码

3. **ANDROID_KEY_ALIAS**
   - 描述：密钥别名

4. **ANDROID_KEY_PASSWORD**
   - 描述：密钥密码

### 如何生成签名密钥
```bash
# 生成 keystore
keytool -genkey -v -keystore keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias \
  -storepass your-keystore-password \
  -keypass your-key-password

# 转换为 base64（用于 GitHub Secrets）
base64 keystore.jks
```

## 本地构建测试
在推送到 GitHub 之前，建议先在本地测试构建：

```bash
# 清理并构建
./gradlew clean build

# 运行测试
./gradlew test

# 代码检查
./gradlew lint

# 构建 Release APK
./gradlew assembleRelease

# 构建 Release AAB
./gradlew bundleRelease
```

## 注意事项

1. **首次运行**：首次运行可能会较慢，因为需要下载 Android SDK 和 Gradle 依赖
2. **缓存**：workflow 配置了 Gradle 缓存，后续运行会更快
3. **Android SDK**：使用 `android-actions/setup-android@v3` 自动设置 Android SDK
4. **Java 版本**：使用 JDK 17，与项目配置一致
5. **构建产物**：构建产物会上传到 GitHub Actions Artifacts，保留 7-30 天

## 手动触发构建
在 GitHub 仓库的 Actions 页面，可以选择 workflow 并点击 "Run workflow" 手动触发构建。