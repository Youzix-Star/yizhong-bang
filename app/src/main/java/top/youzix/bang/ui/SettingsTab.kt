package top.youzix.bang.ui

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.youzix.bang.BuildConfig
import top.youzix.bang.MainViewModel
import top.youzix.bang.UiState
import top.youzix.bang.data.AppTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun SettingsTab(
    state: UiState,
    viewModel: MainViewModel,
    scrollBehavior: ScrollBehavior,
    onOpenAbout: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 104.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item { SettingsSectionTitle("外观") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
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
                SwitchPreference(
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
                SwitchPreference(
                    title = "纯黑背景",
                    summary = "深色模式下生效",
                    checked = settings.amoled,
                    onCheckedChange = viewModel::setAmoled,
                )
            }
        }

        item { SettingsSectionTitle("数据") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "退出登录",
                    summary = "清除会话并返回登录",
                    onClick = viewModel::logout,
                )
            }
        }

        item { SettingsSectionTitle("其他") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                ArrowPreference(
                    title = "关于一中帮",
                    summary = "v${BuildConfig.VERSION_NAME}",
                    onClick = onOpenAbout,
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}
