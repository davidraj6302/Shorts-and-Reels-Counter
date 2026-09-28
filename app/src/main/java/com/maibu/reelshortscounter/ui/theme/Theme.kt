package com.maibu.reelshortscounter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.maibu.reelshortscounter.AppTheme

// --- COLOR SCHEMES ---

// Default (Purple)
private val DefaultDark = darkColorScheme(
    primary = PurpleAccent,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.Black
)
private val DefaultLight = lightColorScheme(
    primary = Purple40,
    background = Color(0xFFFDFDFD),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White
)

// Instagram
private val InstagramDark = darkColorScheme(
    primary = InstaPink,
    secondary = InstaOrange,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White
)
private val InstagramLight = lightColorScheme(
    primary = InstaPink,
    secondary = InstaOrange,
    background = Color(0xFFFDFDFD),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White
)

// YouTube
private val YouTubeDark = darkColorScheme(
    primary = YouTubeRed,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White
)
private val YouTubeLight = lightColorScheme(
    primary = YouTubeRed,
    background = Color(0xFFFDFDFD),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White
)

// Facebook
private val FacebookDark = darkColorScheme(
    primary = FacebookBlue,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White
)
private val FacebookLight = lightColorScheme(
    primary = FacebookBlue,
    background = Color(0xFFFDFDFD),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White
)

@Composable
fun ScrollSenseTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    isDarkMode: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        appTheme == AppTheme.INSTAGRAM -> if (isDarkMode) InstagramDark else InstagramLight
        appTheme == AppTheme.YOUTUBE -> if (isDarkMode) YouTubeDark else YouTubeLight
        appTheme == AppTheme.FACEBOOK -> if (isDarkMode) FacebookDark else FacebookLight
        else -> if (isDarkMode) DefaultDark else DefaultLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
