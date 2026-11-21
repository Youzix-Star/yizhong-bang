package com.yizhong.bang.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.glass.HomeFill
import top.yukonga.miuix.kmp.icon.glass.ListView
import top.yukonga.miuix.kmp.icon.glass.SettingsFill
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(state: UiState, viewModel: MainViewModel, scope: MiuixComponentScope) {
    var tab by remember { mutableIntStateOf(0) }
    val colors = MiuixTheme.colorScheme

    BackHandler(enabled = tab != 0) { tab = 0 }

    val navItems = listOf(
        YzNavItem(MiuixIcons.Glass.HomeFill, "总览"),
        YzNavItem(MiuixIcons.Glass.ListView, "成绩"),
        YzNavItem(MiuixIcons.Glass.SettingsFill, "设置"),
    )

    val grade = state.meta?.grades?.firstOrNull { it.value == state.grade }
    val examLabel = grade?.exams?.firstOrNull { it.value == state.exam }?.label.orEmpty()
    val classLabel = grade?.classes?.firstOrNull { it.value == state.clazz }?.label.orEmpty()
    val subtitle = when (tab) {
        0 -> listOfNotNull(state.meta?.school, state.sheet?.meta?.term).joinToString(" · ")
        1 -> listOfNotNull(examLabel, classLabel).joinToString(" · ")
        else -> "外观、数据与关于"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            YzTopAppBar(
                title = when (tab) {
                    0 -> "总览"
                    1 -> "成绩"
                    else -> "设置"
                },
                subtitle = subtitle.ifBlank { "一中帮" },
                useOs4 = scope.useOs4,
                backdrop = scope.contentBackdrop,
                actions = {
                    YzTopBarAction(
                        icon = MiuixIcons.Refresh,
                        contentDescription = "刷新",
                        useOs4 = scope.useOs4,
                        backdrop = scope.contentBackdrop,
                        onClick = viewModel::refresh,
                    )
                },
            )
        },
        bottomBar = {
            YzNavBar(
                items = navItems,
                selected = tab,
                useOs4 = scope.useOs4,
                backdrop = scope.contentBackdrop,
                onSelect = { tab = it },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(scope.contentBackdropModifier),
        ) {
            if (state.busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.error?.let { message ->
                ErrorBanner(message = message, onRetry = { viewModel.loadSheet(force = true) })
            }
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    0 -> OverviewTab(state, viewModel)
                    1 -> ScoresTab(state, viewModel)
                    else -> SettingsTab(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        cornerRadius = 14.dp,
        insideMargin = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 8.dp,
            top = 10.dp,
            bottom = 10.dp,
        ),
    ) {
        Text(
            text = message,
            fontSize = 12.5.sp,
            color = colors.error,
        )
        TextButton(
            text = "重试",
            onClick = onRetry,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** 给 LazyColumn 用的小卡片外壳。 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        insideMargin = androidx.compose.foundation.layout.PaddingValues(18.dp),
        content = content,
    )
}

/** 简单的分组小标题。 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.5.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
