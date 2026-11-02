package com.example.myguidance

import androidx.compose.ui.graphics.Color

data class StatusColors(val container: Color, val content: Color)

enum class StatusButtonPreset(
    val status: String,
    val label: String,
    val clickable: Boolean,
    private val light: StatusColors,
    private val dark: StatusColors
) {
    // Main action (solid blue)
    TBS(
        "TBS", "Set Schedule", true,
        light = StatusColors(Color(0xFF1565C0), Color.White),
        dark = StatusColors(Color(0xFF90CAF9), Color(0xFF0A1F36))
    ),
    // Waiting (soft amber, not clickable)
    PENDING(
        "Pending", "Pending", false,
        light = StatusColors(Color(0xFFFFF1CC), Color(0xFF5C4300)),
        dark = StatusColors(Color(0xFF5C4300), Color(0xFFFFE9A8))
    ),
    // Final step (solid green)
    SCHEDULED(
        "Scheduled", "Set as Resolved", true,
        light = StatusColors(Color(0xFF2E7D32), Color.White),
        dark = StatusColors(Color(0xFFA5D6A7), Color(0xFF10260F))
    ),
    // Done (soft green, not clickable)
    RESOLVED(
        "Resolved", "Resolved", false,
        light = StatusColors(Color(0xFFE8F5E9), Color(0xFF1B5E20)),
        dark = StatusColors(Color(0xFF1B4D1F), Color(0xFFD7F0D8))
    ),
    // Reopen action (solid orange)
    DISMISSED(
        "Dismissed", "Re-evaluate", true,
        light = StatusColors(Color(0xFFF57C00), Color.White),
        dark = StatusColors(Color(0xFFFFB74D), Color(0xFF2B1700))
    ),

    DISMISS(
        "TBS", "Dismiss", true,
        light = StatusColors(Color(0xFFF44336), Color.White),
        dark = StatusColors(Color(0xFCFF204F), Color(0xFF2B0000))
    );

    fun colorsFor(isDark: Boolean): StatusColors = if (isDark) dark else light

    companion object {
        fun fromStatus(status: String): StatusButtonPreset? =
            entries.firstOrNull { it.status.equals(status, ignoreCase = true) }
    }
}