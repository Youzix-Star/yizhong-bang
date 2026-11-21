package com.yizhong.bang.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yizhong.bang.BuildConfig
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import com.yizhong.bang.data.AppTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsTab(state: UiState, viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MiuixTheme.colorScheme

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("外观") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                YzSpinnerPreference(
                    items = AppTheme.values().map {
                        DropdownItem(
                            title = when (it) {
                                AppTheme.System -> "跟随系统"
                                AppTheme.Light -> "浅色"
                                AppTheme.Dark -> "深色"
                            },
                        )
                    },
                    selectedIndex = settings.theme.ordinal,
                    title = "主题",
                    onSelectedIndexChange = { index ->
                        AppTheme.values().getOrNull(index)?.let(viewModel::setTheme)
                    },
                )
                SettingSwitch(
                    title = "动态取色",
                    summary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "跟随壁纸的主色，Android 12 及以上"
                    } else {
                        "需要 Android 12 及以上"
                    },
                    checked = settings.dynamicColor,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    onCheckedChange = viewModel::setDynamicColor,
                )
                SettingSwitch(
                    title = "纯黑背景",
                    summary = "只在深色模式生效，OLED 更省电",
                    checked = settings.amoled,
                    onCheckedChange = viewModel::setAmoled,
                )
            }
        }

        item { SectionTitle("数据") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                BasicComponent(
                    title = "数据源",
                    summary = "学校成绩系统 · 只读",
                )
                BasicComponent(
                    title = "缓存",
                    summary = "本次会话内一直保留，点右上角刷新才更新",
                )
                BasicComponent(
                    title = "退出登录",
                    summary = "清掉这次会话，重新粘链接",
                    onClick = viewModel::logout,
                )
            }
        }

        item { SectionTitle("答题卡") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                BasicComponent(
                    title = "试卷分析链接",
                    summary = if (state.answerBound) "已绑定，可以查看答卷图片" else "未绑定（在登录页粘贴试卷分析链接）",
                )
                BasicComponent(
                    title = "图片保存",
                    summary = "答题卡查看后保存到相册 / Pictures/一中帮",
                )
                val answerError = state.answerError
                if (!answerError.isNullOrBlank()) {
                    BasicComponent(
                        title = "上次绑定失败",
                        summary = answerError,
                    )
                }
            }
        }

        item { SectionTitle("关于") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                BasicComponent(
                    title = "版本",
                    summary = "v${BuildConfig.VERSION_NAME}",
                )
                BasicComponent(
                    title = "设计参考",
                    summary = "HyperOS 4 · Miuix · InstallerX Revived",
                )
                BasicComponent(
                    title = "隐私",
                    summary = "成绩只在本机内存里，token 不写日志、不落盘",
                )
            }
        }

        if (state.demo) {
            item {
                SectionCard {
                    Text(
                        text = "示例模式",
                        fontSize = 13.sp,
                        color = colors.primary,
                    )
                    Text(
                        text = "这 58 名学生的姓名已经换成「学生01」这种占位符，不会访问学校服务器。",
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        item {
            Text(
                text = "一中帮 · 给自己看的成绩单",
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    BasicComponent(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = { if (enabled) onCheckedChange(!checked) },
        endActions = {
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) },
                enabled = enabled,
            )
        },
    )
}
