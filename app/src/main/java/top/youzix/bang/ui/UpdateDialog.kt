package top.youzix.bang.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.bang.data.UpdateInfo
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun UpdateDialog(info: UpdateInfo, onDismiss: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    val context = LocalContext.current
    var showBackup by remember { mutableStateOf(false) }

    WindowDialog(
        show = true,
        title = "发现新版本 v${info.version}",
        summary = "选择更新源",
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (info.notes.isNotBlank()) {
                Text(
                    text = info.notes,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            SourceCard(
                title = "蓝奏云",
                summary = "提取码：3oac",
                onClick = { openUrl(context, "https://wwaya.lanzouv.com/b00zz4gtbc") },
            )
            SourceCard(
                title = "GitHub Release",
                summary = "在 GitHub 下载最新 APK",
                onClick = { openUrl(context, info.releaseUrl) },
            )

            TextButton(
                text = if (showBackup) "收起备用更新源" else "备用更新源",
                onClick = { showBackup = !showBackup },
                modifier = Modifier.fillMaxWidth(),
            )

            if (showBackup) {
                SourceCard(
                    title = "小飞机云盘",
                    summary = "备用下载",
                    onClick = { openUrl(context, "https://share.feijipan.com/s/cwzRCZZk") },
                )
                SourceCard(
                    title = "123 云盘",
                    summary = "提取码：iFun",
                    onClick = {
                        openUrl(
                            context,
                            "https://1819243723.share.123pan.cn/123pan/ugM9jv-gQMTA?pwd=iFun#",
                        )
                    },
                )
            }

            TextButton(
                text = "稍后",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SourceCard(title: String, summary: String, onClick: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick,
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
        )
        Text(
            text = summary,
            fontSize = 11.5.sp,
            color = colors.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

private fun openUrl(context: Context, url: String) {
    if (url.isBlank()) return
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
