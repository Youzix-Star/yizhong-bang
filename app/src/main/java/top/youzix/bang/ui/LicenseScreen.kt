package top.youzix.bang.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowDialog

private data class LicenseItem(
    val name: String,
    val author: String,
    val license: String,
    val summary: String,
    val url: String,
)

private val Licenses = listOf(
    LicenseItem(
        name = "Miuix",
        author = "compose-miuix-ui",
        license = "Apache-2.0",
        summary = "界面组件、主题与玻璃效果。",
        url = "https://github.com/compose-miuix-ui/miuix",
    ),
    LicenseItem(
        name = "AndroidX / Jetpack Compose",
        author = "Google",
        license = "Apache-2.0",
        summary = "Compose 运行时、Activity 与生命周期。",
        url = "https://developer.android.com/jetpack/androidx",
    ),
    LicenseItem(
        name = "OkHttp",
        author = "Square",
        license = "Apache-2.0",
        summary = "访问学校系统的 HTTP 客户端。",
        url = "https://square.github.io/okhttp/",
    ),
    LicenseItem(
        name = "Kotlin & kotlinx.coroutines",
        author = "JetBrains",
        license = "Apache-2.0",
        summary = "Kotlin 语言与协程运行时。",
        url = "https://kotlinlang.org/",
    ),
    LicenseItem(
        name = "InstallerX Revived",
        author = "wxxsfxyzm 与贡献者",
        license = "GPL-3.0",
        summary = "动态背景、关于页结构与导航转场的参考实现。",
        url = "https://github.com/wxxsfxyzm/InstallerX-Revived",
    ),
    LicenseItem(
        name = "一中帮",
        author = "Youzix-Star",
        license = "GPL-3.0",
        summary = "本应用自身，源码公开在 GitHub。",
        url = "https://github.com/Youzix-Star/yizhong-bang",
    ),
)

@Composable
fun LicenseScreen(onClose: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    val context = LocalContext.current
    var selected by remember { mutableStateOf<LicenseItem?>(null) }

    PredictiveBackContainer(onBack = onClose) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            SmallTopAppBar(
                title = "开源许可",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "本应用在以下开源项目的基础上构建。点击项目可查看许可证与项目主页。",
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                items(Licenses) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        pressFeedbackType = PressFeedbackType.Sink,
                        showIndication = true,
                        onClick = { selected = item },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = item.license,
                                    fontSize = 10.sp,
                                    color = colors.primary,
                                    modifier = Modifier
                                        .padding(start = 8.dp),
                                )
                            }
                            Text(
                                text = item.author,
                                fontSize = 11.5.sp,
                                color = colors.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                            Text(
                                text = item.summary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = colors.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "许可证文本版权归各项目作者所有。",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                item { Spacer(Modifier.height(200.dp)) }
            }
        }
    }

    selected?.let { item ->
        WindowDialog(
            show = true,
            title = item.name,
            summary = "${item.license} · ${item.author}",
            onDismissRequest = { selected = null },
            onDismissFinished = { selected = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = item.summary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.onSurface,
                )
                Text(
                    text = "完整许可证文本可在项目主页查看。",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        text = "关闭",
                        onClick = { selected = null },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = "打开许可证",
                        onClick = { openUrl(context, item.url) },
                        modifier = Modifier.weight(1f),
                    )
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
