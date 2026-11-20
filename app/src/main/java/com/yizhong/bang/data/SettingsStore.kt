package com.yizhong.bang.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme { System, Light, Dark }

data class AppSettings(
    val theme: AppTheme = AppTheme.System,
    val dynamicColor: Boolean = true,
    val amoled: Boolean = false,
    val gold: Boolean = false,
)

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("yz_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun setTheme(theme: AppTheme) = update { it.copy(theme = theme) }
    fun setDynamicColor(enabled: Boolean) = update { it.copy(dynamicColor = enabled) }
    fun setAmoled(enabled: Boolean) = update { it.copy(amoled = enabled) }
    fun setGold(enabled: Boolean) = update { it.copy(gold = enabled) }

    private fun update(block: (AppSettings) -> AppSettings) {
        val next = block(_state.value)
        _state.value = next
        prefs.edit()
            .putString("theme", next.theme.name)
            .putBoolean("dynamic", next.dynamicColor)
            .putBoolean("amoled", next.amoled)
            .putBoolean("gold", next.gold)
            .apply()
    }

    private fun read(): AppSettings = AppSettings(
        theme = runCatching {
            AppTheme.valueOf(prefs.getString("theme", AppTheme.System.name) ?: AppTheme.System.name)
        }.getOrDefault(AppTheme.System),
        dynamicColor = prefs.getBoolean("dynamic", true),
        amoled = prefs.getBoolean("amoled", false),
        gold = prefs.getBoolean("gold", false),
    )
}
