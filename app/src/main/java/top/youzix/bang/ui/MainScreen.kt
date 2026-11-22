package top.youzix.bang.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.youzix.bang.MainViewModel
import top.youzix.bang.UiState
import top.youzix.bang.data.Student
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.glass.HomeFill
import top.yukonga.miuix.kmp.icon.glass.ListView
import top.yukonga.miuix.kmp.icon.glass.SettingsFill
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(
    state: UiState,
    viewModel: MainViewModel,
    scope: MiuixComponentScope,
    isTop: Boolean,
    onOpenAbout: () -> Unit,
    onOpenAnswerSheet: (Student, Int) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val colors = MiuixTheme.colorScheme
    val scrollBehavior = MiuixScrollBehavior()

    BackHandler(enabled = isTop && tab != 0) { tab = 0 }

    LaunchedEffect(tab) {
        scrollBehavior.state?.let {
            it.contentOffset = 0f
            it.heightOffset = 0f
            it.overscrollOffset = 0f
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                scrollBehavior = scrollBehavior,
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
                    0 -> OverviewTab(state, viewModel, scrollBehavior)
                    1 -> ScoresTab(
                        state = state,
                        viewModel = viewModel,
                        scrollBehavior = scrollBehavior,
                        onAnswerSheet = onOpenAnswerSheet,
                    )
                    else -> SettingsTab(
                        state = state,
                        viewModel = viewModel,
                        scrollBehavior = scrollBehavior,
                        onOpenAbout = onOpenAbout,
                    )
                }
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

/** 分组小标题交给 Miuix 的 SmallTitle，字号和缩进跟系统设置页一致。 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    SmallTitle(
        text = text,
        modifier = modifier,
        textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        insideMargin = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 6.dp,
            vertical = 2.dp,
        ),
    )
}
