package com.yizhong.bang.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

/**
 * 二级页面的预测式返回：手势拖动时整页跟着位移、缩放，
 * 松手完成才真正关闭；取消则回到原位。
 */
@Composable
fun PredictiveBackContainer(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = true) { events ->
        try {
            events.collect { event -> progress = event.progress }
            onBack()
        } catch (e: CancellationException) {
            progress = 0f
            throw e
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = progress.coerceIn(0f, 1f)
                translationX = size.width * 0.28f * p
                scaleX = 1f - 0.08f * p
                scaleY = 1f - 0.08f * p
                alpha = 1f - 0.10f * p
            },
        content = content,
    )
}
