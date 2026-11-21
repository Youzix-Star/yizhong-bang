package com.yizhong.bang.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yizhong.bang.BuildConfig
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import com.yizhong.bang.data.AppTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsTab(
    state: UiState,
    viewModel: MainViewModel,
    onOpenAbout: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MiuixTheme.colorScheme

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        "跟随壁纸主色"
                    } else {
                        "需要 Android 12"
                    },
                    checked = settings.dynamicColor,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    onCheckedChange = viewModel::setDynamicColor,
                )
                SettingSwitch(
                    title = "纯黑背景",
                    summary = "深色模式下生效",
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
                    title = "退出登录",
                    summary = "清除会话并返回登录",
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
                    title = "连接状态",
                    summary = if (state.answerBound) "已绑定" else "未绑定",
                )
                val answerError = state.answerError
                if (!answerError.isNullOrBlank()) {
                    BasicComponent(
                        title = "上次错误",
                        summary = answerError,
                    )
                }
            }
        }

        item { SectionTitle("其他") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                BasicComponent(
                    title = "关于一中帮",
                    summary = "v${BuildConfig.VERSION_NAME}",
                    onClick = onOpenAbout,
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.ChevronForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colors.onSurfaceVariantSummary,
                        )
                    },
                )
            }
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
