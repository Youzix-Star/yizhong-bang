package top.youzix.bang.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 会话内缓存：取过一次就一直留着，只有点刷新（force = true）才重新请求。
 * 终端是 App，不是网页，没必要按分钟过期。会话本身仍只活在进程里，不落盘。
 */
data class OpenedSession(
    val user: String?,
    val answerSheetUrl: String?,
)

class GradeRepository {

    private val mutex = Mutex()
    private var client = SchoolClient()

    private var metaCache: Meta? = null
    private val sheetCache = mutableMapOf<String, Sheet>()

    suspend fun open(url: String): OpenedSession = withContext(Dispatchers.IO) {
        val fresh = SchoolClient()
        val user = fresh.openLink(url)
        val answerSheetUrl = fresh.answerSheetUrl
        mutex.withLock {
            client = fresh
            metaCache = null
            sheetCache.clear()
        }
        OpenedSession(user, answerSheetUrl)
    }

    suspend fun meta(force: Boolean = false): Meta = withContext(Dispatchers.IO) {
        mutex.withLock {
            val cached = metaCache
            if (!force && cached != null) return@withLock cached.copy(cached = true)
            val fresh = client.meta().copy(cached = false)
            metaCache = fresh
            fresh
        }
    }

    suspend fun sheet(
        grade: String,
        exam: String,
        clazz: String,
        force: Boolean = false,
    ): Sheet = withContext(Dispatchers.IO) {
        mutex.withLock {
            val key = "$grade|$exam|$clazz"
            val hit = sheetCache[key]
            if (!force && hit != null) return@withLock hit.copy(cached = true)
            val fresh = client.sheet(grade, exam, clazz).copy(
                cached = false,
                at = System.currentTimeMillis(),
            )
            sheetCache[key] = fresh
            fresh
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        mutex.withLock {
            client = SchoolClient()
            metaCache = null
            sheetCache.clear()
        }
    }
}
