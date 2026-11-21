package com.yizhong.bang.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.BuildConfig
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
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
        Column(modifier = Modifier.fillMaxSize()) {
            SmallTopAppBar(
                title = "关于",
                subtitle = "v${BuildConfig.VERSION_NAME}",
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
                    top = 8.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        insideMargin = PaddingValues(24.dp),
                        colors = CardDefaults.defaultColors(
                            color = colors.surfaceContainerHigh,
                        ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LogoMark(64.dp)
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = "一中帮",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.onSurface,
                                )
                                Text(
                                    text = "城固一中 · 成绩查询",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = "只读的成绩与答题卡查看工具，数据只留在这台手机上。",
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            color = colors.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }

                item { SectionTitle("信息") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        insideMargin = PaddingValues(vertical = 4.dp),
                    ) {
                        BasicComponent(
                            title = "版本",
                            summary = "v${BuildConfig.VERSION_NAME}",
                        )
                        BasicComponent(
                            title = "构建",
                            summary = "GitHub Actions · 公开构建",
                        )
                    }
                }

                item { SectionTitle("设计") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        insideMargin = PaddingValues(vertical = 4.dp),
                    ) {
                        BasicComponent(
                            title = "界面",
                            summary = "HyperOS 4 · Miuix",
                        )
                        BasicComponent(
                            title = "参考",
                            summary = "InstallerX Revived",
                        )
                    }
                }

                item { SectionTitle("隐私") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
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
                            lineHeight = 18.sp,
                            color = colors.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        insideMargin = PaddingValues(vertical = 4.dp),
                    ) {
                        BasicComponent(
                            title = "GitHub 仓库",
                            summary = "Youzix-Star/yizhong-bang",
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
