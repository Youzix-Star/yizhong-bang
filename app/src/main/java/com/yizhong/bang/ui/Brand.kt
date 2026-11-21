package com.yizhong.bang.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.data.SubjectRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 「一 + 三根上升的柱子」的校徽。全部用 Canvas 画，不需要图片资源。 */
@Composable
fun LogoMark(size: Dp = 48.dp) {
    val primary = MiuixTheme.colorScheme.primary
    val onPrimary = MiuixTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(size * 0.3f)

    Canvas(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.72f)))),
    ) {
        val w = this.size.width
        val h = this.size.height
        val ink = onPrimary
        drawLine(
            color = ink,
            start = Offset(w * 0.24f, h * 0.33f),
            end = Offset(w * 0.76f, h * 0.33f),
            strokeWidth = w * 0.075f,
            cap = StrokeCap.Round,
        )
        fun bar(left: Float, top: Float, height: Float, alpha: Float) {
            drawRoundRect(
                color = ink.copy(alpha = alpha),
                topLeft = Offset(w * left, h * top),
                size = Size(w * 0.11f, h * height),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
            )
        }
        bar(0.24f, 0.60f, 0.16f, 0.82f)
        bar(0.445f, 0.51f, 0.25f, 0.92f)
        bar(0.65f, 0.42f, 0.34f, 1f)
    }
}

@Composable
fun StatsTile(label: String, value: String, unit: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (unit != null) {
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                )
            }
        }
    }
}

@Composable
fun DeltaBadge(delta: Double?, modifier: Modifier = Modifier) {
    if (delta == null) {
        Text("暂无年级均分", fontSize = 11.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = modifier)
        return
    }
    val good = delta >= 0.5
    val bad = delta <= -0.5
    val color = when {
        good -> MiuixTheme.colorScheme.primary
        bad -> MiuixTheme.colorScheme.error
        else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    val background = when {
        good -> MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
        bad -> MiuixTheme.colorScheme.error.copy(alpha = 0.12f)
        else -> MiuixTheme.colorScheme.surfaceContainerHigh
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = "${if (delta > 0) "+" else ""}${com.yizhong.bang.data.Stats.fmt(delta, 1)} 较年级",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

/** 九科「本班 vs 年级」的迷你柱状图，纯 Compose 布局。 */
@Composable
fun SubjectBars(rows: List<SubjectRow>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        rows.forEach { row ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height((82f * row.gradePct).coerceAtLeast(2f).dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f)),
                        )
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height((82f * row.clsPct).coerceAtLeast(2f).dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MiuixTheme.colorScheme.primary),
                        )
                    }
                }
                Text(
                    text = row.name,
                    fontSize = 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
