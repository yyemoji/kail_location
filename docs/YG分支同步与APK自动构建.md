# YG 分支同步与 APK 自动构建

本文说明 YG 分支（取消登录/次数限制的定制分支）如何同步上游 `main`，以及合并后如何拿到最新的本体与 Xposed 插件 APK。

---

## 一、固定下载链接（永不变）

每次构建成功后，下面两个链接会自动指向最新一次 YG 构建产物，**收藏一次即可长期使用**：

- 软件本体：<https://github.com/yyemoji/kail_location/releases/download/yg-latest/KailLocation.apk>
- Xposed 插件：<https://github.com/yyemoji/kail_location/releases/download/yg-latest/KailLocationXposed.apk>
- 校验值：<https://github.com/yyemoji/kail_location/releases/download/yg-latest/SHA256SUMS.txt>

发布页（可查看构建来源的 commit）：<https://github.com/yyemoji/kail_location/releases/tag/yg-latest>

> 该 Release 标记为 prerelease，标签 `yg-latest` 会被强制移动到最新构建的提交，产物用 `--clobber` 覆盖，所以链接地址恒定不变。

---

## 二、触发方式

### 1. 自动触发（推荐，日常就用这个）

工作流监听 **YG 分支的 push**。也就是说：

```
只要把 YG 推到 GitHub，构建就会自动开始
```

不需要点任何按钮。

### 2. 手动触发（代码没变、只想重新构建时）

GitHub 仓库 → **Actions** → 左侧 **Build latest YG APKs** → 右上 **Run workflow** → 分支选 `YG` → **Run workflow**。

### 3. 并发说明

同一分支的多次推送共用并发组 `yg-apks-*`，新推送会**取消**上一次尚未完成的运行，不会堆积。

---

## 三、同步上游 main 的标准流程

```bash
# 0) 进入工程目录
cd /d D:\Code\kail_locationyy

# 1) 确保当前在 YG 分支且工作区干净
git switch YG
git status

# 2) 拉取上游 main（二选一）
#    a. 上游就在本仓库的 origin/main（GitHub 网页上先点 “Sync fork” 同步过 main）
git fetch origin main
#    b. 上游是另一个远端，例如原作者仓库
# git remote add upstream <上游仓库地址>
# git fetch upstream main

# 3) 把 main 合并进 YG
git merge origin/main      # 或 git merge upstream/main

# 4) 如果有冲突，见下一节的冲突处理原则

# 5) 推送 —— 这一步就会自动触发云端构建
git push origin YG

# 6) 约 5~10 分钟后，直接下载固定链接即可
```

### 冲突处理原则

定制的“取消登录/订阅/次数限制”逻辑**已收敛到唯一入口**：

```
app/src/main/java/com/kail/location/auth/UsageManager.kt
```

冲突高发也正是这个文件。处理规则：

- `canStartSimulation()` / `consumeSimulation()`：**必须保留 YG 的无条件放行实现**（直接 `return true`，不查询登录态、不请求服务端、不扣次数）。
- 同文件里的开机就绪门控（`systemReadiness()` / `BOOT_READY_THRESHOLD_MS`）属于**安全逻辑，必须保留上游版本**。
- 其余模块（登录页、个人资料、订阅/赞助页）按上游 `main` 走，YG 只是不再把它们作为使用前置条件。

### 构建内置的回归校验

CI 在打包前会先跑 `:app:testDebugUnitTest`，其中的 `SimulationAccessPolicyTest` 专门断言：

- 无账号上下文也能 `canStartSimulation()`
- 启动模拟不会扣减次数

如果合并 `main` 时把登录/次数限制又带回来了，**构建会直接失败、不会发布**，从而避免发出一版“看着能用、其实要登录”的 APK。

---

## 四、查看构建结果

- 运行列表：<https://github.com/yyemoji/kail_location/actions/workflows/build-apks.yml>
- 单次运行里还有 `KailLocation-YG-APKs` 工件（保留 30 天），Release 则是长期固定入口。
- 失败时优先看失败的那一步日志；若为依赖仓库报错（例如某个镜像 502），检查 `settings.gradle.kts` 中的仓库配置。

---

## 五、相关文件

| 文件 | 作用 |
| --- | --- |
| `.github/workflows/build-apks.yml` | 自动构建 + 发布 Release 的工作流 |
| `settings.gradle.kts` | 仓库源配置；国内镜像仅本地生效，CI 只用官方源 |
| `app/src/main/java/com/kail/location/auth/UsageManager.kt` | 登录/次数限制的唯一收口处 |
| `app/src/test/java/com/kail/location/auth/SimulationAccessPolicyTest.kt` | 定制逻辑的回归测试 |
