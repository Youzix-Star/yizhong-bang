# 一中帮

[![build](https://github.com/Youzix-Star/yizhong-bang/actions/workflows/build.yml/badge.svg)](https://github.com/Youzix-Star/yizhong-bang/actions/workflows/build.yml)

城固一中成绩查询系统的 Android 客户端，只读、自用。界面用 [Miuix](https://github.com/compose-miuix-ui/miuix) 的 OS4 / Glass 组件，部分编排参考 [InstallerX Revived](https://github.com/wxxsfxyzm/InstallerX-Revived)。

构建放在 GitHub Actions，本地不跑 Gradle。

## 功能

- 粘贴「成绩分析」链接登录，自动找到「试卷分析」入口。
- 班级总览：班均分、年级差值、参考人数、偏科人次、九科对比。
- 成绩列表：总分排序、姓名/考号筛选、逐科得分、班均、班位、级位。
- 答题卡：按科目翻页查看，双指缩放；可保存单页/全部，也可把所有科目打包成 ZIP（内置 README）。
- 主题：跟随系统 / 浅色 / 深色、动态取色、纯黑背景。
- 启动自动检测 GitHub Release，弹窗提供蓝奏云、GitHub、备用云盘等更新源。
- 首次启动需输入免责协议原文后才能进入。
- 示例模式，数据已脱敏，不访问学校服务器。

## 安装

最新 Release：

<https://github.com/Youzix-Star/yizhong-bang/releases/latest>

也可以在 Actions 里下载 `yizhong-bang-apk`（debug）或 `yizhong-bang-release`（release）。

Release 使用仓库 Secrets 中的签名构建；签名文件不入库。

## 构建

只走 GitHub Actions：[.github/workflows/build.yml](.github/workflows/build.yml)

- JDK 25、Android SDK 37、Gradle 9.9.1、AGP 9.5.1
- 跑 `testDebugUnitTest assembleDebug`，有签名 Secrets 时再跑 `assembleRelease`
- 版本号由 run number 注入，形如 `1.0.53`

## 兼容性

- `minSdk 26`（Android 8）。
- 玻璃/模糊效果需要 Android 13；低版本自动退回普通 Miuix 外观。
- 答题卡保存到相册需要 Android 10；旧系统保存到应用外部目录。
- ZIP 导出需要 Android 10 及以上。

## 目录

```
app/src/main/java/top/youzix/bang/
├── MainActivity.kt
├── MainViewModel.kt
├── data/
│   ├── SchoolClient.kt
│   ├── SheetParser.kt
│   ├── Html.kt
│   ├── GradeRepository.kt
│   ├── AnswerSheetRepository.kt
│   ├── UpdateChecker.kt
│   ├── Stats.kt
│   └── DemoData.kt
└── ui/
    ├── Theme.kt
    ├── Host.kt
    ├── AppNav.kt
    ├── GateScreen.kt
    ├── AgreementScreen.kt
    ├── MainScreen.kt
    ├── OverviewTab.kt
    ├── ScoresTab.kt
    ├── AnswerSheetScreen.kt
    ├── SettingsTab.kt
    ├── AboutScreen.kt
    └── LicenseScreen.kt
```

## 隐私

- 登录链接里的 token 只存在内存，不写日志、不落盘、不上传。
- App 直接连接学校服务器，没有中转。
- 查询结果包含学生姓名、考号、成绩和答题卡图片，只适合自己使用；不要公开部署或分享带 token 的链接。
- 仓库中的示例数据已脱敏。

## 已知限制

- 只支持「学生成绩通知单」和「学生答卷查询」。
- 答题卡的图片模板目前对应 2026-2027 上学期期中；换考试后需要更新模板。
- 会话只存在内存，杀进程后要重新粘贴链接。
- 学校 token 过期后需要重新从浏览器复制。
- 没有做账号密码登录（学校需要验证码）。

## 许可

- InstallerX Revived：GPL-3.0，OS4 界面编排与部分实现参考。
- Miuix：Apache-2.0，Compose 组件、玻璃与模糊。
- HyperOS 4 / MIUI：仅视觉参考，无隶属关系。

本项目以 GPL-3.0 发布，见 [LICENSE](LICENSE)。
