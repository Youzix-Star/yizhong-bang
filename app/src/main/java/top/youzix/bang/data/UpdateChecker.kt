package top.youzix.bang.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import top.youzix.bang.BuildConfig

data class UpdateInfo(
    val version: String,
    val notes: String,
    val releaseUrl: String,
)

object UpdateChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun check(): UpdateInfo? = checkResult().getOrNull()

    suspend fun checkResult(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://api.github.com/repos/Youzix-Star/yizhong-bang/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "YiZhongBang/${BuildConfig.VERSION_NAME}")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = JSONObject(response.body?.string() ?: return@use null)
                val tag = json.optString("tag_name").removePrefix("v").trim()
                if (tag.isBlank() || !isNewer(tag, BuildConfig.VERSION_NAME)) return@use null
                UpdateInfo(
                    version = tag,
                    notes = json.optString("body"),
                    releaseUrl = json.optString("html_url")
                        .ifBlank { "https://github.com/Youzix-Star/yizhong-bang/releases" },
                )
            }
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val a = latest.split(Regex("[^0-9]+")).filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
        val b = current.split(Regex("[^0-9]+")).filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
        val size = maxOf(a.size, b.size)
        for (i in 0 until size) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
