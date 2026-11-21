package com.yizhong.bang.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.yizhong.bang.data.AppTheme
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController

private val LocalIsDark = staticCompositionLocalOf { false }

object YzTheme {
    val isDark: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalIsDark.current
}

@Composable
fun YzTheme(
    theme: AppTheme,
    dynamicColor: Boolean,
    amoled: Boolean,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (theme) {
        AppTheme.System -> systemDark
        AppTheme.Light -> false
        AppTheme.Dark -> true
    }

    val monet = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val mode = if (monet) {
        when (theme) {
            AppTheme.System -> ColorSchemeMode.MonetSystem
            AppTheme.Light -> ColorSchemeMode.MonetLight
            AppTheme.Dark -> ColorSchemeMode.MonetDark
        }
    } else {
        when (theme) {
            AppTheme.System -> ColorSchemeMode.System
            AppTheme.Light -> ColorSchemeMode.Light
            AppTheme.Dark -> ColorSchemeMode.Dark
        }
    }

    val controller = remember(mode, dark) {
        ThemeController(
            colorSchemeMode = mode,
            isDark = dark,
            colorSpec = ThemeColorSpec.Spec2025,
        )
    }

    CompositionLocalProvider(LocalIsDark provides dark) {
        MiuixTheme(controller = controller) {
            if (amoled && dark) {
                val colors = MiuixTheme.colorScheme
                MiuixTheme(
                    colors = colors.copy(
                        background = Color.Black,
                        surface = Color.Black,
                        surfaceContainer = Color.Black,
                    ),
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}
