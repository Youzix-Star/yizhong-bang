package com.yizhong.bang.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.AnswerState
import com.yizhong.bang.AnswerTarget
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.ZoomOut
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AnswerSheetScreen(
    state: AnswerState,
    subjects: List<String>,
    onSelectSubject: (Int) -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    val target = state.target ?: return
    val context = LocalContext.current
    val colors = MiuixTheme.colorScheme
    val pages = state.pages
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val tabsState = rememberLazyListState()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var stageSize by remember { mutableStateOf(IntSize.Zero) }
    var saving by remember { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }

    LaunchedEffect(target.studentNo, target.subjectIndex, pages.isNotEmpty()) {
        // 换人/换科目时回到第一页，避免沿用上一份答卷的页码和缩放。
        if (pages.isNotEmpty()) pagerState.scrollToPage(0)
        scale = 1f
        offset = Offset.Zero
    }

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offset = Offset.Zero
    }

    LaunchedEffect(target.subjectIndex, subjects.size) {
        if (subjects.size > 1 && target.subjectIndex in subjects.indices) {
            tabsState.animateScrollToItem(target.subjectIndex)
        }
    }

    LaunchedEffect(state.error, pages.size) {
        val message = state.error
        if (!message.isNullOrBlank() && pages.isNotEmpty()) {
            val result = snackbar.showSnackbar(
                message = message,
                actionLabel = "重试",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onRetry()
        }
    }

    fun zoomBy(factor: Float) {
        val next = (scale * factor).coerceIn(1f, 6f)
        scale = next
        offset = if (next <= 1.001f) Offset.Zero else clampOffset(offset, next, stageSize)
    }

    fun saveCurrent() {
        if (saving || pages.isEmpty()) return
        val index = pagerState.currentPage.coerceIn(0, pages.lastIndex)
        val snapshot = listOf(pages[index])
        saving = true
        scope.launch {
            val count = try {
                savePages(context, snapshot, target, index)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                0
            }
            saving = false
            snackbar.showSnackbar(
                message = if (count > 0) {
                    "已保存到相册 · Pictures/一中帮"
                } else {
                    "保存失败，稍后再试"
                },
            )
        }
    }

    fun saveAll() {
        if (saving || pages.isEmpty()) return
        val snapshot = pages.toList()
        saving = true
        scope.launch {
            val count = try {
                savePages(context, snapshot, target, 0)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                0
            }
            saving = false
            snackbar.showSnackbar(
                message = if (count > 0) {
                    "已保存 $count 张到相册 · Pictures/一中帮"
                } else {
                    "保存失败，稍后再试"
                },
            )
        }
    }

    val subtitle = buildString {
        append(target.className.ifBlank { "答题卡" })
        if (pages.isNotEmpty()) {
            append(" · 第 ")
            append(pagerState.currentPage + 1)
            append("/")
            append(pages.size)
            append(" 页")
        }
        if (state.loading) {
            append(" · 载入中")
        } else if (!state.error.isNullOrBlank() && pages.isNotEmpty()) {
            append(" · 只取到 ${pages.size} 页")
        }
    }

    PredictiveBackContainer(onBack = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            SmallTopAppBar(
                title = "${target.studentName} · ${target.subject}",
                subtitle = subtitle,
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = MiuixIcons.ChevronBackward,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                actions = {
                    if (state.loading) {
                        InfiniteProgressIndicator(
                            color = colors.primary,
                            size = 20.dp,
                            strokeWidth = 2.dp,
                            orbitingDotSize = 2.dp,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                    }
                },
            )

            if (subjects.size > 1) {
                TabRow(
                    tabs = subjects,
                    selectedTabIndex = target.subjectIndex.coerceIn(0, subjects.lastIndex),
                    onTabSelected = onSelectSubject,
                    listState = tabsState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colors.surfaceContainer),
            ) {
                when {
                    pages.isEmpty() && state.loading -> LoadingPanel(
                        target = target,
                        onClose = onClose,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    pages.isEmpty() && !state.error.isNullOrBlank() -> ErrorPanel(
                        message = state.error.orEmpty(),
                        onClose = onClose,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    pages.isEmpty() -> EmptyPanel(
                        onClose = onClose,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    else -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            pageSpacing = 10.dp,
                            beyondViewportPageCount = 1,
                        ) { page ->
                            ZoomableAnswer(
                                bytes = pages[page],
                                scale = scale,
                                offset = offset,
                                size = stageSize,
                                onScale = { scale = it },
                                onOffset = { offset = it },
                                onSize = { stageSize = it },
                            )
                        }

                        if (state.loading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                height = 3.dp,
                            )
                        }

                        if (pages.size > 1) {
                            PagePill(
                                current = pagerState.currentPage,
                                total = pages.size,
                                onPrev = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            (pagerState.currentPage - 1).coerceAtLeast(0),
                                        )
                                    }
                                },
                                onNext = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex),
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp),
                            )
                        }
                    }
                }
            }

            if (pages.isNotEmpty()) {
                AnswerBottomBar(
                    scale = scale,
                    saving = saving,
                    loading = state.loading,
                    onZoomOut = { zoomBy(1f / 1.35f) },
                    onZoomIn = { zoomBy(1.35f) },
                    onResetZoom = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    onSaveCurrent = ::saveCurrent,
                    onSaveAll = ::saveAll,
                )
            }
        }

            SnackbarHost(
                state = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (pages.isNotEmpty()) 78.dp else 18.dp),
            )
        }
    }
}

@Composable
private fun LoadingPanel(
    target: AnswerTarget,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        cornerRadius = 24.dp,
        insideMargin = PaddingValues(24.dp),
        colors = CardDefaults.defaultColors(color = colors.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InfiniteProgressIndicator(
                color = colors.primary,
                size = 48.dp,
                strokeWidth = 3.dp,
                orbitingDotSize = 5.dp,
            )
            Text(
                text = "正在拿取答题卡",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = "正在向学校服务器取图，第一页出来就能先看。",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "${target.subject} · ${target.className}",
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 12.dp),
            )
            TextButton(
                text = "取消",
                onClick = onClose,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ErrorPanel(
    message: String,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        cornerRadius = 24.dp,
        insideMargin = PaddingValues(24.dp),
        colors = CardDefaults.defaultColors(color = colors.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "答题卡没取到",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
            Text(
                text = message,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(text = "返回", onClick = onClose)
                Button(onClick = onRetry) {
                    Text("重试", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyPanel(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "这个科目还没有答题卡",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
        )
        TextButton(
            text = "返回",
            onClick = onClose,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun PagePill(
    current: Int,
    total: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = modifier,
        cornerRadius = 50.dp,
        insideMargin = PaddingValues(horizontal = 2.dp),
        colors = CardDefaults.defaultColors(color = colors.surfaceContainerHigh),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrev,
                enabled = current > 0,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = MiuixIcons.ChevronBackward,
                    contentDescription = "上一页",
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "${current + 1} / $total",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            IconButton(
                onClick = onNext,
                enabled = current < total - 1,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = MiuixIcons.ChevronForward,
                    contentDescription = "下一页",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AnswerBottomBar(
    scale: Float,
    saving: Boolean,
    loading: Boolean,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onResetZoom: () -> Unit,
    onSaveCurrent: () -> Unit,
    onSaveAll: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        cornerRadius = 22.dp,
        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onZoomOut,
                enabled = !saving,
                modifier = Modifier.size(40.dp),
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.ZoomOut,
                    contentDescription = "缩小",
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "${(scale * 100).roundToInt()}%",
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(42.dp)
                    .clickable(onClickLabel = "重置缩放", onClick = onResetZoom)
                    .padding(vertical = 12.dp),
            )
            IconButton(
                onClick = onZoomIn,
                enabled = !saving,
                modifier = Modifier.size(40.dp),
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "放大",
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            val saveTextStyle = MiuixTheme.textStyles.button.copy(fontSize = 13.sp)
            TextButton(
                text = "保存本页",
                onClick = onSaveCurrent,
                enabled = !saving,
                insideMargin = PaddingValues(horizontal = 6.dp),
                textStyle = saveTextStyle,
            )
            Button(
                onClick = onSaveAll,
                enabled = !saving,
                insideMargin = PaddingValues(horizontal = 10.dp),
            ) {
                if (saving) {
                    InfiniteProgressIndicator(
                        color = colors.onPrimary,
                        size = 16.dp,
                        strokeWidth = 2.dp,
                        orbitingDotSize = 2.dp,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = when {
                        saving -> "保存中"
                        loading -> "保存已加载"
                        else -> "保存全部"
                    },
                    style = saveTextStyle,
                )
            }
        }
    }
}

@Composable
private fun ZoomableAnswer(
    bytes: ByteArray,
    scale: Float,
    offset: Offset,
    size: IntSize,
    onScale: (Float) -> Unit,
    onOffset: (Offset) -> Unit,
    onSize: (IntSize) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    var bitmap by remember(bytes) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(bytes) { mutableStateOf(false) }

    LaunchedEffect(bytes) {
        val decoded = withContext(Dispatchers.IO) { decodeAnswer(bytes) }
        if (decoded == null) failed = true else bitmap = decoded
    }

    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged(onSize)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image == null) {
            if (failed) {
                Text(
                    text = "图片解码失败",
                    fontSize = 13.sp,
                    color = colors.error,
                )
            } else {
                InfiniteProgressIndicator(
                    color = colors.primary,
                    size = 30.dp,
                    strokeWidth = 2.5.dp,
                    orbitingDotSize = 3.dp,
                )
            }
        } else {
            val transformState = rememberTransformableState { zoom, pan, _ ->
                val next = (currentScale * zoom).coerceIn(1f, 6f)
                onScale(next)
                onOffset(
                    if (next <= 1.001f) {
                        Offset.Zero
                    } else {
                        clampOffset(currentOffset + pan, next, size)
                    },
                )
            }
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(
                        state = transformState,
                        canPan = { scale > 1.001f },
                    )
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
            )
        }
    }
}

private fun clampOffset(offset: Offset, scale: Float, size: IntSize): Offset {
    val maxX = size.width * (scale - 1f) / 2f
    val maxY = size.height * (scale - 1f) / 2f
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY),
    )
}

private fun decodeAnswer(bytes: ByteArray): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.RGB_565
        inSampleSize = 2
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

private suspend fun savePages(
    context: Context,
    pages: List<ByteArray>,
    target: AnswerTarget,
    startPage: Int,
): Int = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    var saved = 0
    pages.forEachIndexed { index, bytes ->
        val page = startPage + index + 1
        val safe = "${target.studentName}_${target.subject}_$page"
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "答题卡_$safe.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/一中帮")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@forEachIndexed
        try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("无法写入相册")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            saved += 1
        } catch (e: Exception) {
            runCatching { resolver.delete(uri, null, null) }
        }
    }
    saved
}
