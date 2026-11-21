package com.yizhong.bang.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val GroupTopShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
private val GroupBottomShape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
private val GroupShape = RoundedCornerShape(18.dp)

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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SettingsSectionTitle("外观") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
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
                    modifier = Modifier.clip(GroupTopShape),
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
                    modifier = Modifier.clip(GroupBottomShape),
                    onCheckedChange = viewModel::setAmoled,
                )
            }
        }

        item { SettingsSectionTitle("数据") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
            ) {
                BasicComponent(
                    title = "退出登录",
                    summary = "清除会话并返回登录",
                    modifier = Modifier.clip(GroupShape),
                    onClick = viewModel::logout,
                )
            }
        }

        item { SettingsSectionTitle("答题卡") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
            ) {
                val answerError = state.answerError
                val hasError = !answerError.isNullOrBlank()
                BasicComponent(
                    title = "连接状态",
                    summary = if (state.answerBound) "已绑定" else "未绑定",
                    modifier = Modifier.clip(if (hasError) GroupTopShape else GroupShape),
                )
                if (!answerError.isNullOrBlank()) {
                    BasicComponent(
                        title = "上次错误",
                        summary = answerError,
                        modifier = Modifier.clip(GroupBottomShape),
                    )
                }
            }
        }

        item { SettingsSectionTitle("其他") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
            ) {
                BasicComponent(
                    title = "关于一中帮",
                    summary = "v${BuildConfig.VERSION_NAME}",
                    modifier = Modifier.clip(GroupShape),
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
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.5.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    BasicComponent(
        modifier = modifier,
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
