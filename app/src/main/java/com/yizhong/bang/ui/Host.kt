package com.yizhong.bang.ui

import android.os.Build
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.glass.GlassDropdownPreference
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.isRenderEffectSupported
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Immutable
data class MiuixComponentScope(
    val useOs4: Boolean = false,
    val contentBackdrop: LayerBackdrop? = null,
    val pageBackdrop: LayerBackdrop? = null,
)

internal val LocalMiuixComponentScope = compositionLocalOf { MiuixComponentScope() }

val MiuixComponentScope.contentBackdropModifier: Modifier
    get() = contentBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier

val MiuixComponentScope.pageBackdropModifier: Modifier
    get() = pageBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier

/**
 * OS4 的根容器：建立两块采样层，一块给顶栏/底栏，一块给弹出的玻璃菜单。
 */
@Composable
fun MiuixHost(
    useOs4: Boolean,
    useBlur: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    content: @Composable (MiuixComponentScope) -> Unit,
) {
    val useGlass = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        useOs4 && isRenderEffectSupported()
    val contentBackdrop = rememberMiuixBlurBackdrop(useGlass || useBlur)
    val pageBackdrop = rememberMiuixBlurBackdrop(useGlass)
    val scope = remember(useGlass, contentBackdrop, pageBackdrop) {
        MiuixComponentScope(useGlass, contentBackdrop, pageBackdrop)
    }

    CompositionLocalProvider(LocalMiuixComponentScope provides scope) {
        Scaffold(
            modifier = modifier,
            containerColor = containerColor,
            contentWindowInsets = WindowInsets(0),
        ) {
            content(scope)
        }
    }
}

/** OS4 用玻璃顶栏，老系统退回普通 Miuix 顶栏。 */
@Composable
fun YzTopAppBar(
    title: String,
    subtitle: String,
    useOs4: Boolean,
    backdrop: LayerBackdrop?,
    scrollBehavior: ScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (useOs4) {
        GlassTopAppBar(
            title = title,
            largeTitle = title,
            subtitle = subtitle,
            backdrop = backdrop,
            progressiveBlur = backdrop != null,
            scrollBehavior = scrollBehavior,
            style = GlassDefaults.style(YzTheme.isDark),
            actions = actions,
        )
    } else {
        TopAppBar(
            title = title,
            largeTitle = title,
            subtitle = subtitle,
            scrollBehavior = scrollBehavior,
            color = MiuixTheme.colorScheme.surface,
            actions = actions,
        )
    }
}

@Composable
fun YzTopBarAction(
    icon: ImageVector,
    contentDescription: String,
    useOs4: Boolean,
    backdrop: LayerBackdrop?,
    onClick: () -> Unit,
) {
    if (useOs4) {
        GlassIconButton(onClick = onClick, backdrop = backdrop) {
            Icon(icon, contentDescription, Modifier.size(24.dp))
        }
    } else {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription, Modifier.size(24.dp))
        }
    }
}

@Immutable
data class YzNavItem(val icon: ImageVector, val label: String)

/** 底部导航：OS4 是悬浮玻璃胶囊，其他系统是 Miuix 导航栏。 */
@Composable
fun YzNavBar(
    items: List<YzNavItem>,
    selected: Int,
    useOs4: Boolean,
    backdrop: LayerBackdrop?,
    onSelect: (Int) -> Unit,
) {
    if (useOs4 && backdrop != null) {
        GlassNavigationBar(
            items = items.map { GlassNavigationItem(icon = it.icon, label = it.label) },
            selectedIndex = selected,
            onSelect = onSelect,
            backdrop = backdrop,
            style = GlassDefaults.style(YzTheme.isDark),
            modifier = Modifier.padding(horizontal = 18.dp),
        )
    } else {
        NavigationBar(color = MiuixTheme.colorScheme.surface) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selected == index,
                    onClick = { onSelect(index) },
                    icon = item.icon,
                    label = item.label,
                )
            }
        }
    }
}

/** 筛选下拉：OS4 变成玻璃弹层，老系统仍是 Miuix 的滚轮。 */
@Composable
fun YzSpinnerPreference(
    items: List<DropdownItem>,
    selectedIndex: Int,
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onSelectedIndexChange: (Int) -> Unit,
) {
    val scope = LocalMiuixComponentScope.current
    val plain = items.all {
        it.enabled && it.icon == null && it.summary == null && it.children.isNullOrEmpty()
    }
    if (scope.useOs4 && plain) {
        GlassDropdownPreference(
            onSelectedIndexChange = { index ->
                onSelectedIndexChange(index)
                items.getOrNull(index)?.onClick?.invoke()
            },
            items = items.map { it.text },
            selectedIndex = selectedIndex,
            title = title,
            backdrop = scope.pageBackdrop,
            visuals = GlassPopupDefaults.visuals(style = GlassDefaults.style(YzTheme.isDark)),
            modifier = modifier,
            enabled = enabled,
            showValue = true,
            renderInRootScaffold = true,
            summary = summary,
        )
    } else {
        WindowSpinnerPreference(
            items = items,
            selectedIndex = selectedIndex,
            title = title,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            showValue = true,
            onSelectedIndexChange = onSelectedIndexChange,
        )
    }
}
