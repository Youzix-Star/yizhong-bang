package top.youzix.bang.ui.egg

/**
 * 《朋友的酒》果冻曲线的 Kotlin 移植，数值与模组 JellyAnimation.java 一致。
 *
 * 来源：https://github.com/jiaotan798/friendswine（All Rights Reserved，已获作者授权用于本彩蛋）。
 */
object JellyMotion {

    /** 一次完整回弹（压两次）的时长，秒。 */
    const val LENGTH = 0.91667

    /** 音乐循环长度：2482560 帧 / 44100 Hz。 */
    const val MUSIC_LENGTH = 2482560.0 / 44100.0

    private const val MUSIC_FADE = 0.1

    private val TIMES = doubleArrayOf(0.0, 0.25, 0.45833, 0.70833, LENGTH)
    private val VALUES = doubleArrayOf(0.0, 1.0, 0.0, 1.0, 0.0)

    fun phase(seconds: Double): Double = seconds.coerceAtLeast(0.0) % LENGTH

    /** 0…1 的压扁量：极值之间用 smoothstep，等价于 Blockbench 里切线为 0 的 Catmull-Rom。 */
    fun squash(seconds: Double): Float {
        val time = phase(seconds)
        var i = 0
        while (i < TIMES.size - 2 && time > TIMES[i + 1]) i++
        val u = (time - TIMES[i]) / (TIMES[i + 1] - TIMES[i])
        val p1 = VALUES[i]
        val p2 = VALUES[i + 1]
        return (p1 + (p2 - p1) * u * u * (3 - 2 * u)).toFloat()
    }

    /** 音乐版 squash：按曲长循环，最后 0.1 秒收尾，避免接缝跳变。 */
    fun musicSquash(seconds: Double): Float {
        val time = seconds.coerceAtLeast(0.0) % MUSIC_LENGTH
        var value = squash(time)
        val tail = (time - (MUSIC_LENGTH - MUSIC_FADE)) / MUSIC_FADE
        if (tail > 0) value *= (1 - tail * tail * (3 - 2 * tail)).toFloat()
        return value
    }

    fun angle(seconds: Double): Float = (-360.0 * phase(seconds) / LENGTH).toFloat()
}

/** 三位角色在本彩蛋里用到的图集布局（已缩放到 android 尺寸）。 */
enum class EggGuest(
    val id: String,
    val frames: Int,
    val columns: Int,
    val rows: Int,
    val loopMs: Int,
    val frameWidth: Int,
    val frameHeight: Int,
    val aspect: Float,
) {
    KASUMI("kasumi", 99, 10, 10, 9900, 120, 120, 1f),
    EMMA("emma", 21, 5, 5, 440, 80, 93, 159f / 185f),
    AMIYA("amiya", 27, 6, 5, 2700, 200, 200, 1f);

    val atlasPath: String get() = "egg/guests/$id/dance-atlas.png"

    /** 选帧规则：艾玛 20 ms/帧、末帧 40 ms，其余 100 ms/帧。 */
    fun frameIndex(seconds: Double): Int {
        if (!seconds.isFinite() || seconds <= 0.0) return 0
        val millis = seconds * 1000.0 % loopMs
        val step = if (this == EMMA) 20.0 else 100.0
        return minOf(frames - 1, (millis / step).toInt())
    }
}
