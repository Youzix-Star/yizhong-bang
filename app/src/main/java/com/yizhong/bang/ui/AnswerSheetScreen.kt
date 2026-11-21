package com.yizhong.bang.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.AnswerState
import com.yizhong.bang.AnswerTarget
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.launch

@Composable
fun AnswerSheetScreen(
    state: AnswerState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    val target = state.target ?: return
    val context = LocalContext.current
    val colors = MiuixTheme.colorScheme
    val pages = state.pages
    val error = state.error
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = MiuixIcons.ChevronBackward,
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${target.studentName} · ${target.subject}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = "${target.className} · 答题卡",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
            if (pages.isNotEmpty()) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pages.size}",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading -> Text(
                    text = "正在取答题卡…",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.align(Alignment.Center),
                )

                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = colors.error,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(
                        text = "重试",
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                pages.isEmpty() -> Text(
                    text = "没有答题卡",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    ZoomableAnswer(pages[page])
                }
            }
        }

        if (pages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    text = "上一页",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                )
                TextButton(
                    text = "下一页",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex),
                            )
                        }
                    },
                    enabled = pagerState.currentPage < pages.lastIndex,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = "保存本页",
                    onClick = {
                        val index = pagerState.currentPage.coerceIn(0, pages.lastIndex)
                        val count = save(context, listOf(pages[index]), target, index)
                        toastSave(context, count)
                    },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val count = save(context, pages, target, 0)
                        toastSave(context, count)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存全部", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ZoomableAnswer(bytes: ByteArray) {
    val options = remember {
        BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = 2
        }
    }
    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) }
    if (bitmap == null) {
        Text("图片解码失败", fontSize = 13.sp, color = MiuixTheme.colorScheme.error)
        return
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )
    }
}

private fun save(
    context: Context,
    pages: List<ByteArray>,
    target: AnswerTarget,
    startPage: Int,
): Int {
    val resolver = context.contentResolver
    var saved = 0
    pages.forEachIndexed { index, bytes ->
        val safe = "${target.studentName}_${target.subject}_${startPage + index + 1}"
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "答题卡_$safe.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/一中帮")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@forEachIndexed
        resolver.openOutputStream(uri)?.use { it.write(bytes) }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        saved += 1
    }
    return saved
}

private fun toastSave(context: Context, count: Int) {
    Toast.makeText(
        context,
        if (count > 0) "已保存 $count 张到相册 / Pictures/一中帮" else "保存失败",
        Toast.LENGTH_SHORT,
    ).show()
}
