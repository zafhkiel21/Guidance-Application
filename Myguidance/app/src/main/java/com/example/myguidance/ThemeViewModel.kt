package com.example.myguidance

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _preset = MutableStateFlow(
        runCatching {
            ThemePreset.valueOf(prefs.getString("preset", ThemePreset.OCEAN.name)!!)
        }.getOrDefault(ThemePreset.OCEAN)
    )
    val preset: StateFlow<ThemePreset> = _preset.asStateFlow()

    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _accent = MutableStateFlow(
        runCatching {
            AccentPreset.valueOf(prefs.getString("accent", AccentPreset.MATCH.name)!!)
        }.getOrDefault(AccentPreset.MATCH)
    )
    val accent: StateFlow<AccentPreset> = _accent.asStateFlow()

    fun select(preset: ThemePreset) {
        _preset.value = preset
        prefs.edit { putString("preset", preset.name) }
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        prefs.edit { putBoolean("dark_mode", enabled)}
    }

    fun selectAccent(accent: AccentPreset) {
        _accent.value = accent
        prefs.edit { putString("accent", accent.name)}
    }
}