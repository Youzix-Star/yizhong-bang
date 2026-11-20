package com.yizhong.bang.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** 带 10 分钟内存缓存的取数入口。会话只活在进程里，不落盘。 */
class GradeRepository {

    private val mutex = Mutex()
    private var client = SchoolClient()

    private var metaCache: Meta? = null
    private var metaAt = 0L
    private val sheetCache = mutableMapOf<String, Pair<Long, Sheet>>()

    suspend fun open(url: String): String? = withContext(Dispatchers.IO) {
        val fresh = SchoolClient()
        val user = fresh.openLink(url)
        mutex.withLock {
            client = fresh
            metaCache = null
            metaAt = 0L
            sheetCache.clear()
        }
        user
    }

    suspend fun meta(force: Boolean = false): Meta = withContext(Dispatchers.IO) {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val cached = metaCache
            if (!force && cached != null && now - metaAt < TTL) {
                return@withLock cached.copy(cached = true)
            }
            val fresh = client.meta().copy(cached = false)
            metaCache = fresh
            metaAt = now
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
            val now = System.currentTimeMillis()
            val hit = sheetCache[key]
            if (!force && hit != null && now - hit.first < TTL) {
                return@withLock hit.second.copy(cached = true, at = hit.first)
            }
            val fresh = client.sheet(grade, exam, clazz).copy(cached = false, at = now)
            sheetCache[key] = now to fresh
            fresh
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        mutex.withLock {
            client = SchoolClient()
            metaCache = null
            metaAt = 0L
            sheetCache.clear()
        }
    }

    private companion object {
        const val TTL = 10 * 60 * 1000L
    }
}
