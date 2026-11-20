# 一中帮

[![build](https://github.com/Youzix-Star/yizhong-bang/actions/workflows/build.yml/badge.svg)](https://github.com/Youzix-Star/yizhong-bang/actions/workflows/build.yml)

城固一中「成绩查询分析系统」的安卓客户端：**OS4 玻璃风格**，只读，自己用。

学校那套 ASP.NET WebForms 页面又长又难用，所以之前让 AI 做过一个网页版。
这一版把它搬成原生 Android，界面用 [Miuix](https://github.com/compose-miuix-ui/miuix)
的 Glass / OS4 组件重做，OS4 的组件编排方式参考了
[InstallerX Revived](https://github.com/wxxsfxyzm/InstallerX-Revived)。

> 构建全部在 GitHub Actions 完成，本地不跑 Gradle。APK 见 Actions 的 `yizhong-bang-apk`。

---

## 能做什么

- **粘链接进入**：把浏览器地址栏里带 `token` 的那条链接粘进来，App 自己找到「成绩查询」入口。
- **班级总览**：班均分、与年级均分的差值、参考人数、偏科人次，外加一句人话点评。
- **九科对比**：每科按自己的满分归一化，本班 vs 年级并排看。
- **成绩列表**：总分排序、按姓名 / 考号筛选、点开某一行看逐科得分、班均、位次。
- **OS4 外观**：顶栏、底部胶囊导航、下拉筛选、弹层全部是玻璃材质。Miuix 的 blur 模块硬性要求 Android 13（API 33）及以上。
- **主题**：跟随系统 / 浅色 / 深色、动态取色（Android 12+）、纯黑背景。
- **示例模式**：58 名学生的姓名已替换成「学生01」这种占位符，不连学校服务器。
- **只读**：没有任何写操作。

## 界面结构

```
总览 tab ── 班均分 / 差值 / 三条指标 / 九科柱状图 / 筛选
成绩 tab ── 搜索 + 学生卡片（点开即展开九科明细）
设置 tab ── 主题 / 动态取色 / 纯黑 / 退出登录 / 关于
```

## OS4 是怎么做的

- 依赖 `top.yukonga.miuix.kmp:miuix-glass` 的 `GlassTopAppBar`、`GlassNavigationBar`、
  `GlassDropdownPreference`、`GlassIconButton`，以及 `miuix-blur` 的 `LayerBackdrop`。
- 页面内容录进一块 `LayerBackdrop`，顶栏和底栏采样它做真实的背景模糊；弹层再单独录一层，
  避免采样到自己的弹窗。
- 玻璃只用于「外壳」：顶栏、底部导航、下拉、动作按钮。内容卡片仍是 Miuix 的实色卡片，
  这样中文长文本和成绩数字才清楚。
- 运行时着色器要求 **Android 13（API 33）**；`minSdk` 因此也是 33。
- 主题用 Miuix `ThemeController`，支持 Material You 动态色与 SPEC_2025 调色。

## 怎么装

1. 打开本仓库的 **Actions** → 最新的 `build` → 页面底部 `yizhong-bang-apk`。
2. 下载、解压，得到 `app-debug.apk`，在手机上安装（需要允许「安装未知来源应用」）。
3. 打开 App，把成绩系统链接整个粘进去，点「进入」。
4. 只想看排版：点「先看看示例」。

命令行下载（需要 `gh`）：

```bash
gh run download --repo Youzix-Star/yizhong-bang --name yizhong-bang-apk
```

## 构建

只有一条路：GitHub Actions（[.github/workflows/build.yml](.github/workflows/build.yml)）。

- JDK 25 + Android SDK 37 + Gradle 9.9.1 + AGP 9.5.1
- 先跑 `testDebugUnitTest`，再 `assembleDebug`
- 产物：`app/build/outputs/apk/debug/app-debug.apk`
- 版本号由 run number 注入，形如 `1.0.42`

本地构建不是设计目标；如果真要跑，需要同样的 JDK / SDK，并自行承担环境差异。

## 目录

```
app/src/main/java/com/yizhong/bang/
├── MainActivity.kt          入口、主题、采样层
├── MainViewModel.kt         会话与取数状态
├── data/
│   ├── SchoolClient.kt      cookie + __VIEWSTATE + postback
│   ├── SheetParser.kt       导出 HTML → Sheet
│   ├── Html.kt              表单 / 下拉框 / 实体解析
│   ├── GradeRepository.kt   10 分钟内存缓存
│   ├── Stats.kt             差值、点评、筛选排序
│   └── DemoData.kt          示例数据
└── ui/
    ├── Theme.kt             Miuix ThemeController
    ├── Host.kt              OS4 顶栏 / 底栏 / 弹层编排
    ├── GateScreen.kt        粘链接入口
    ├── MainScreen.kt        三个 tab 的壳
    ├── OverviewTab.kt       总览
    ├── ScoresTab.kt         成绩列表
    └── SettingsTab.kt       设置
```

## 隐私与安全

- 链接里的 `token` 就是登录凭证。App 只在内存里用它，**不写日志、不落盘、不上传**。
- App 只从这台手机直连学校服务器（HTTP，学校那边本来就没 HTTPS），没有中转服务器。
- 学校返回的是一个班 58 名学生的姓名、考号、逐科成绩和排名。**只适合自己用，别公开部署、别分享链接。**
- 仓库里的示例数据已脱敏。
- 没有实现「账号密码登录」：学校那套还需要验证码，属于没实测过的路径，不如粘链接稳。

## 彩蛋

设置 → 关于 → 连续点「版本」7 次。

## 已知限制

- 只做了「学生成绩通知单」，侧边栏的「历次成绩跟踪」「答卷报告单」还没接。
- 会话在内存里，杀进程后要重新粘一次链接。
- 学校 `token` 过期后需要重新从浏览器复制。
- 只支持 Android 13 及以上（Miuix blur 的硬性要求）。

## 参考与许可

- **InstallerX Revived**（GPL-3.0）：OS4 玻璃界面的组件编排参考并改编自该项目。
- **Miuix**（Apache-2.0）：Compose 组件、玻璃材质、模糊与运行时着色器。
- **HyperOS 4 / MIUI**：仅视觉参考，无隶属关系。

本项目以 **GPL-3.0** 发布，见 [LICENSE](LICENSE) 与 [NOTICE](NOTICE)。
