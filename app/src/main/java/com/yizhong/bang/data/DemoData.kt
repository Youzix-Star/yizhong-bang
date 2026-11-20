package com.yizhong.bang.data

import android.content.Context
import kotlinx.serialization.json.Json

/** 脱敏示例：不连学校服务器，用来预览排版。 */
object DemoData {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun load(context: Context): DemoPayload {
        val text = context.assets.open("demo.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        return json.decodeFromString(DemoPayload.serializer(), text)
    }
}
