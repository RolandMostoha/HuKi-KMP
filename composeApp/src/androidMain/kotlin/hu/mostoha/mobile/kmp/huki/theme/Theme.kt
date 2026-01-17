package hu.mostoha.mobile.kmp.huki.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.ui.components.toComposeColor

@Composable
private fun lightColors(): ColorScheme {
    val context = LocalContext.current
    return lightColorScheme(
        primary = SharedRes.colors.primary.toComposeColor(context),
        onPrimary = SharedRes.colors.onPrimary.toComposeColor(context),
    )
}

@Composable
private fun darkColors(): ColorScheme {
    val context = LocalContext.current
    return darkColorScheme(
        primary = SharedRes.colors.primary.toComposeColor(context),
        onPrimary = SharedRes.colors.onPrimary.toComposeColor(context),
    )
}

@Composable
fun HuKiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> darkColors()
        else -> lightColors()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
