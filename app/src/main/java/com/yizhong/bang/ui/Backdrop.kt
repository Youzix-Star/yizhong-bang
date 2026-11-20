package com.yizhong.bang.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 录制一块可以给玻璃采样的背景。
 * 先铺一层 surface 颜色，避免半透明内容叠在透明层上发黑。
 */
@Composable
fun rememberMiuixBlurBackdrop(enableBlur: Boolean): LayerBackdrop? {
    if (!enableBlur || !isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun LayerBackdrop?.appBarColor(): Color =
    this?.let { Color.Transparent } ?: MiuixTheme.colorScheme.surface
