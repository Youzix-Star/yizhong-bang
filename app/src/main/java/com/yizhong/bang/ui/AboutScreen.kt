package com.yizhong.bang.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.BuildConfig
import com.yizhong.bang.ui.effect.BgEffectBackground
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen(
    onClose: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val scrollProgress by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            if (index > 0) 1f else (offset / 140f).coerceIn(0f, 1f)
        }
    }
    val barAlpha by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = tween(200),
    )

    PredictiveBackContainer(onBack = onClose) {
        Box(modifier = Modifier.fillMaxSize()) {
            AuroraBackground()
            BgEffectBackground(
                dynamicBackground = true,
                modifier = Modifier.fillMaxSize(),
                effectBackground = true,
                isOs3Effect = true,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = statusBarTop + 280.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    item { SmallTitle("关于") }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp),
                            cornerRadius = 18.dp,
                        ) {
                            BasicComponent(
                                title = "GitHub 仓库",
                                summary = "查看源码",
                                onClick = {
                                    openUrl(context, "https://github.com/Youzix-Star/yizhong-bang")
                                },
                                endActions = {
                                    Icon(
                                        imageVector = MiuixIcons.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colors.onSurfaceVariantSummary,
                                    )
                                },
                            )
                            BasicComponent(
                                title = "构建记录",
                                summary = "GitHub Actions",
                                onClick = {
                                    openUrl(
                                        context,
                                        "https://github.com/Youzix-Star/yizhong-bang/actions",
                                    )
                                },
                            )
                            BasicComponent(
                                title = "开源许可",
                                summary = "使用的开源项目与许可证",
                                onClick = onOpenLicenses,
                            )
                            BasicComponent(
                                title = "设计参考",
                                summary = "HyperOS 4 · Miuix · InstallerX Revived",
                            )
                        }
                    }
                }

                // 固定头部：滚动时淡出、缩小；内容从它下面滑过。
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = statusBarTop + 80.dp)
                        .graphicsLayer {
                            alpha = 1f - scrollProgress
                            scaleX = 1f - 0.04f * scrollProgress
                            scaleY = 1f - 0.04f * scrollProgress
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LogoMark(88.dp)
                    Text(
                        text = "一中帮",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} · 城固一中",
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface.copy(alpha = barAlpha)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "返回",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "关于",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface.copy(alpha = barAlpha),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(48.dp))
                    }
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
