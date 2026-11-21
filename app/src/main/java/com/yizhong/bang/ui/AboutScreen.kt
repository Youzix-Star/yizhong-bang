package com.yizhong.bang.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen(onClose: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        BgEffectBackground(
            dynamicBackground = true,
            modifier = Modifier.fillMaxSize(),
            effectBackground = true,
            isOs3Effect = true,
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SmallTopAppBar(
                title = "关于",
                subtitle = "一中帮",
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
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
                            fontSize = 30.sp,
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

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                    ) {
                        BasicComponent(
                            title = "GitHub 仓库",
                            summary = "查看源码与构建记录",
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
                            title = "设计参考",
                            summary = "HyperOS 4 · Miuix · InstallerX Revived",
                            modifier = Modifier.clip(
                                RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                            ),
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        insideMargin = PaddingValues(18.dp),
                    ) {
                        Text(
                            text = "隐私",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "成绩、考号和 token 只在本机内存里使用，不上传、不写日志。",
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            color = colors.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            text = "答题卡照片只保存在相册的 Pictures/一中帮。",
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            color = colors.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 3.dp),
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
        }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
