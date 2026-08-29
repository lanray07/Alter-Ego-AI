package com.alteregoai.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Background = Color(0xFF070812)
private val Surface = Color(0xFF121326)
private val Elevated = Color(0xFF1C1B38)
private val Cyan = Color(0xFF8CFFFF)
private val Blue = Color(0xFF4D8CFF)
private val Purple = Color(0xFF6E3AC5)
private val Success = Color(0xFF4DE69A)
private val Warning = Color(0xFFFFC13D)

private val CinematicDark = darkColorScheme(primary = Cyan, onPrimary = Color(0xFF001313), secondary = Blue, tertiary = Purple, background = Background, surface = Surface, surfaceVariant = Elevated, onSurface = Color.White, onBackground = Color.White, error = Color(0xFFFF8A8A))
private val NeonLight = lightColorScheme(primary = Color(0xFF006B70), onPrimary = Color.White, secondary = Color(0xFF2D5FAE), tertiary = Color(0xFF5E2EA9), background = Color(0xFFF7F8FF), surface = Color.White, surfaceVariant = Color(0xFFE9E9F4), onSurface = Color(0xFF161624), onBackground = Color(0xFF161624))

object AlterEgoColors {
    val cyan = Cyan
    val blue = Blue
    val purple = Purple
    val success = Success
    val warning = Warning
    val muted = Color.White.copy(alpha = .68f)
    val card = Color.White.copy(alpha = .07f)
}

@Composable
fun AlterEgoTheme(themeSelection: String, content: @Composable () -> Unit) {
    val dark = themeSelection != "Light" && (isSystemInDarkTheme() || themeSelection != "System")
    MaterialTheme(colorScheme = if (dark) CinematicDark else NeonLight, typography = Typography(), content = content)
}

fun ColorScheme.contentMuted() = onSurface.copy(alpha = .68f)
