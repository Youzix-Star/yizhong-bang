package top.youzix.bang.ui.egg

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/** 音符存活时间。 */
private const val NOTE_LIFE_NS = 1_150_000_000L

/** 角色从屏幕外飞到位置上的时间。 */
private const val ENTRY_NS = 780_000_000L

/** 一次果冻回弹的时长。 */
private const val JELLY_NS = 920_000_000L

/** 三连击允许的最大间隔。 */
private const val TRIPLE_NS = 900_000_000L

/**
 * 《我的世界》音符粒子的 25 档配色（音高 0～24），取自 Minecraft Wiki「Note Block」的
 * Pitch 对照表，第 25 档回到第 1 档的颜色。
 */
private val NoteColors = intArrayOf(
    0xFF59E800.toInt(), 0xFF82CE00.toInt(), 0xFFACAC00.toInt(), 0xFFCE8400.toInt(),
    0xFFE85900.toInt(), 0xFFF92E00.toInt(), 0xFFFF0606.toInt(), 0xFFF9002E.toInt(),
    0xFFE80059.toInt(), 0xFFCE0082.toInt(), 0xFFAC00AC.toInt(), 0xFF8200CE.toInt(),
    0xFF5900E8.toInt(), 0xFF2E00F9.toInt(), 0xFF0606FF.toInt(), 0xFF002EF9.toInt(),
    0xFF0059E8.toInt(), 0xFF0082CE.toInt(), 0xFF00ACAC.toInt(), 0xFF00CE82.toInt(),
    0xFF00E859.toInt(), 0xFF00F92E.toInt(), 0xFF06FF06.toInt(), 0xFF2EF900.toInt(),
    0xFF59E800.toInt(),
)

internal class NoteBurst(
    val startAt: Long,
    val color: Int,
    val dx: Float,
    val dy: Float,
    val vx: Float,
    val vy: Float,
    val ay: Float,
)

internal class GuestInstance(
    val guest: EggGuest,
    val spawnAt: Long,
    val delay: Long,
    val x: Float,
    val y: Float,
    val edge: Int,
    val height: Float,
)

private data class GuestSlot(
    val guest: EggGuest,
    val x: Float,
    val y: Float,
    val edge: Int,
    val height: Float,
)

private val GuestPlan = listOf(
    GuestSlot(EggGuest.KASUMI, 0.20f, 0.55f, 0, 96f),
    GuestSlot(EggGuest.AMIYA, 0.80f, 0.54f, 1, 96f),
    GuestSlot(EggGuest.EMMA, 0.50f, 0.67f, 2, 88f),
    GuestSlot(EggGuest.KASUMI, 0.16f, 0.84f, 3, 92f),
    GuestSlot(EggGuest.AMIYA, 0.84f, 0.83f, 4, 92f),
)

/** 彩蛋的时钟与状态；由 AboutScreen 记住，跨头部和覆盖层共享。 */
class EggController {
    internal var now by mutableLongStateOf(0L)
    internal var squashAt by mutableLongStateOf(Long.MIN_VALUE)
    internal var partyAt by mutableLongStateOf(0L)
    internal var clockOn by mutableStateOf(false)
    internal val notes = mutableStateListOf<NoteBurst>()
    internal val guests = mutableStateListOf<GuestInstance>()
    internal var blockCenter by mutableStateOf(Offset.Zero)
    internal var overlayOrigin by mutableStateOf(Offset.Zero)

    private var taps = 0
    private var lastTapAt = 0L

    val party: Boolean get() = partyAt != 0L

    /** 点一下：Q 弹 + 冒音符；连点三下开派对，派对中点一下收场。 */
    fun onTap(density: Float) {
        val t = System.nanoTime()
        if (partyAt != 0L) {
            stopParty()
            return
        }
        squashAt = t
        clockOn = true
        taps = if (t - lastTapAt in 0..TRIPLE_NS) taps + 1 else 1
        lastTapAt = t

        val base = ((t / 1_000_000_000L) % NoteColors.size).toInt()
        repeat(3) { i ->
            notes += NoteBurst(
                startAt = t + i * 45_000_000L,
                color = NoteColors[(base + i * 7) % NoteColors.size],
                dx = (Random.nextFloat() - 0.5f) * 14f * density,
                dy = 0f,
                vx = (Random.nextFloat() - 0.5f) * 90f * density,
                vy = -(360f + Random.nextFloat() * 150f) * density,
                ay = 210f * density,
            )
        }

        if (taps >= 3) {
            taps = 0
            startParty(t)
        }
    }

    private fun startParty(t: Long) {
        partyAt = t
        clockOn = true
        guests.clear()
        GuestPlan.forEachIndexed { index, slot ->
            guests += GuestInstance(
                guest = slot.guest,
                spawnAt = t,
                delay = index * 130_000_000L,
                x = slot.x,
                y = slot.y,
                edge = slot.edge,
                height = slot.height,
            )
        }
    }

    private fun stopParty() {
        partyAt = 0L
        guests.clear()
        taps = 0
    }

    internal fun onFrame(t: Long) {
        now = t
        notes.removeAll { t - it.startAt > NOTE_LIFE_NS }
        val jiggling = squashAt != Long.MIN_VALUE && t - squashAt < JELLY_NS
        if (partyAt == 0L && notes.isEmpty() && !jiggling) clockOn = false
    }
}

/** 音符盒图标：点一下按果冻曲线压扁，音乐响起后跟着节拍呼吸。 */
@Composable
fun NoteBlockMark(controller: EggController, size: Dp = 88.dp) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val bitmap = remember { EggImages.load(context, "egg/note_block.png") }
    val t = controller.now
    val squash = when {
        controller.partyAt != 0L -> JellyMotion.musicSquash((t - controller.partyAt) / 1e9)
        controller.squashAt != Long.MIN_VALUE && t - controller.squashAt < JELLY_NS ->
            JellyMotion.squash((t - controller.squashAt) / 1e9)
        else -> 0f
    }
    Box(
        modifier = Modifier
            .size(size)
            .onGloballyPositioned { controller.blockCenter = it.boundsInRoot().center }
            .pointerInput(Unit) {
                detectTapGestures { controller.onTap(density) }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "音符盒",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 脚底固定：只向上压扁、向两侧变宽。
                        scaleX = 1f + 0.5f * squash
                        scaleY = 1f - 0.5f * squash
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
            )
        }
    }
}

/** 全屏覆盖层：音符粒子和被召唤出来的三位角色。 */
@Composable
fun EggOverlay(controller: EggController) {
    val context = LocalContext.current
    val music = remember { EggMusic(context) }
    val note = remember { EggImages.load(context, "egg/note.png") }
    val clockOn = controller.clockOn

    DisposableEffect(Unit) {
        onDispose { music.stop() }
    }

    LaunchedEffect(controller.partyAt) {
        if (controller.partyAt == 0L) {
            music.stop()
            return@LaunchedEffect
        }
        music.play()
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            EggGuest.entries.forEach { EggImages.load(context, it.atlasPath) }
        }
    }

    // 只在有东西动的时候逐帧推进，静止时不占 CPU。
    LaunchedEffect(clockOn) {
        if (!clockOn) return@LaunchedEffect
        while (controller.clockOn) {
            withFrameNanos { t -> controller.onFrame(t) }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { controller.overlayOrigin = it.boundsInRoot().topLeft },
    ) {
        val t = controller.now
        val block = controller.blockCenter - controller.overlayOrigin

        if (note != null) {
            val side = 22.dp.toPx()
            val dst = max(1, side.roundToInt())
            controller.notes.forEach { n ->
                val age = (t - n.startAt) / 1e9f
                if (age < 0f || age > NOTE_LIFE_NS / 1e9f) return@forEach
                val x = block.x + n.dx + n.vx * age
                val y = block.y + n.dy + n.vy * age + 0.5f * n.ay * age * age
                val fade = when {
                    age < 0.12f -> age / 0.12f
                    age > 0.72f -> ((NOTE_LIFE_NS / 1e9f - age) / 0.43f).coerceIn(0f, 1f)
                    else -> 1f
                }
                drawImage(
                    image = note,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(note.width, note.height),
                    dstOffset = IntOffset((x - side / 2f).roundToInt(), (y - side / 2f).roundToInt()),
                    dstSize = IntSize(dst, dst),
                    alpha = fade,
                    colorFilter = ColorFilter.tint(Color(n.color), BlendMode.Multiply),
                    filterQuality = FilterQuality.None,
                )
            }
        }

        controller.guests.forEach { g ->
            val atlas = EggImages.cached(g.guest.atlasPath) ?: return@forEach
            val since = (t - g.spawnAt - g.delay) / 1e9
            if (since < 0) return@forEach
            val entry = (since / (ENTRY_NS / 1e9)).coerceIn(0.0, 1.0).toFloat()
            val eased = 1f - (1f - entry).pow(3)

            val targetX = g.x * size.width
            val targetY = g.y * size.height
            val startX = when (g.edge) {
                0 -> -size.width * 0.35f
                1 -> size.width * 1.35f
                2 -> targetX
                3 -> -size.width * 0.15f
                else -> size.width * 1.15f
            }
            val startY = when (g.edge) {
                0, 1 -> targetY
                2 -> -size.height * 0.35f
                else -> size.height * 1.2f
            }
            val cx = startX + (targetX - startX) * eased
            val cy = startY + (targetY - startY) * eased

            val beat = JellyMotion.musicSquash(since)
            val scale = (0.55f + 0.45f * eased) * (1.25f - 0.5f * beat)
            val height = g.height.dp.toPx() * scale
            val width = height * g.guest.aspect
            val frame = g.guest.frameIndex(since)
            val col = frame % g.guest.columns
            val row = frame / g.guest.columns
            drawImage(
                image = atlas,
                srcOffset = IntOffset(col * g.guest.frameWidth, row * g.guest.frameHeight),
                srcSize = IntSize(g.guest.frameWidth, g.guest.frameHeight),
                dstOffset = IntOffset((cx - width / 2f).roundToInt(), (cy - height).roundToInt()),
                dstSize = IntSize(max(1, width.roundToInt()), max(1, height.roundToInt())),
                alpha = entry,
            )
        }
    }
}

/** 资源解码与缓存；只在 IO 线程或首帧里调用。 */
private object EggImages {
    private val cache = HashMap<String, ImageBitmap>()

    fun load(context: Context, path: String): ImageBitmap? {
        cache[path]?.let { return it }
        val bitmap = runCatching {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
        if (bitmap != null) cache[path] = bitmap
        return bitmap
    }

    fun cached(path: String): ImageBitmap? = cache[path]
}

/** 循环播放玩偶的曲子；只在前台存在，离开关于页就释放。 */
private class EggMusic(context: Context) {
    private val assets = context.applicationContext.assets
    private var player: MediaPlayer? = null
    private var descriptor: AssetFileDescriptor? = null

    fun play() {
        if (player != null) return
        runCatching {
            val fd = assets.openFd("egg/audio/doll_music.ogg")
            val p = MediaPlayer()
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            p.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            p.isLooping = true
            p.setVolume(0.9f, 0.9f)
            p.setOnPreparedListener { it.start() }
            p.prepareAsync()
            descriptor = fd
            player = p
        }.onFailure {
            stop()
        }
    }

    fun stop() {
        player?.let { p ->
            runCatching { p.stop() }
            runCatching { p.release() }
        }
        player = null
        descriptor?.let { runCatching { it.close() } }
        descriptor = null
    }
}
