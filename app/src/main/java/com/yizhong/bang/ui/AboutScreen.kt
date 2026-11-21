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
import androidx.compose.foundation.layout.getTop
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current
    val statusBarTop = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    val scrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30
        }
    }
    val barAlpha by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
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
                        start = 16.dp,
                        end = 16.dp,
                        top = statusBarTop + 72.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp, bottom = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            LogoMark(88.dp)
                            Text(
                                text = "一中帮",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                modifier = Modifier.padding(top = 14.dp),
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} · 城固一中",
                                fontSize = 12.5.sp,
                                color = colors.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    item { AboutSectionTitle("关于") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                        ) {
                            BasicComponent(
                                title = "GitHub 仓库",
                                summary = "查看源码",
                                modifier = Modifier.clip(
                                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                                ),
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
                                modifier = Modifier.clip(
                                    RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                                ),
                            )
                        }
                    }

                    item { AboutSectionTitle("隐私") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            insideMargin = PaddingValues(18.dp),
                        ) {
                            Text(
                                text = "成绩、考号和 token 只在本机内存里使用，不上传、不写日志。",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "答题卡照片只保存在相册的 Pictures/一中帮。",
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = colors.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }

                    item {
                        Text(
                            text = "只读 · 只给自己用",
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariantSummary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                        )
                    }
                }

                // 悬浮顶栏：初始透明，滚动后淡入标题和背景；内容从它下面滑过。
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

@Composable
private fun AboutSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.5.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
