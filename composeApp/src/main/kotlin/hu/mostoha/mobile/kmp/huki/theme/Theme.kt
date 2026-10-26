package hu.mostoha.mobile.kmp.huki.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.util.toComposeColor

val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * Resolves resources in the in-app theme's night mode; [LocalContext] stays the Activity for libraries that need it.
 */
val LocalThemedContext = staticCompositionLocalOf<Context?> { null }

private fun lightColors(context: Context): ColorScheme =
    lightColorScheme(
        primary = SharedRes.colors.primary.toComposeColor(context),
        onPrimary = SharedRes.colors.onPrimary.toComposeColor(context),
        primaryContainer = SharedRes.colors.primaryContainer.toComposeColor(context),
        secondary = SharedRes.colors.secondary.toComposeColor(context),
        onSecondary = SharedRes.colors.onSecondary.toComposeColor(context),
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        tertiary = LightTertiary,
        onTertiary = LightOnTertiary,
        tertiaryContainer = LightTertiaryContainer,
        onTertiaryContainer = LightOnTertiaryContainer,
        inversePrimary = LightInversePrimary,
        background = LightSurface,
        onBackground = LightOnSurface,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        surfaceTint = LightSurfaceTint,
        inverseSurface = LightInverseSurface,
        inverseOnSurface = LightInverseOnSurface,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        surfaceBright = LightSurfaceBright,
        surfaceDim = LightSurfaceDim,
        surfaceContainerLowest = LightSurfaceContainerLowest,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
    )

private fun darkColors(context: Context): ColorScheme =
    darkColorScheme(
        primary = SharedRes.colors.primary.toComposeColor(context),
        onPrimary = SharedRes.colors.onPrimary.toComposeColor(context),
        primaryContainer = SharedRes.colors.primaryContainer.toComposeColor(context),
        secondary = SharedRes.colors.secondary.toComposeColor(context),
        onSecondary = SharedRes.colors.onSecondary.toComposeColor(context),
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        tertiary = DarkTertiary,
        onTertiary = DarkOnTertiary,
        tertiaryContainer = DarkTertiaryContainer,
        onTertiaryContainer = DarkOnTertiaryContainer,
        inversePrimary = DarkInversePrimary,
        background = DarkSurface,
        onBackground = DarkOnSurface,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceTint = DarkSurfaceTint,
        inverseSurface = DarkInverseSurface,
        inverseOnSurface = DarkInverseOnSurface,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        surfaceBright = DarkSurfaceBright,
        surfaceDim = DarkSurfaceDim,
        surfaceContainerLowest = DarkSurfaceContainerLowest,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighest,
    )

@Composable
fun HuKiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val baseContext = LocalContext.current
    val themedContext = remember(baseContext, darkTheme) { baseContext.withNightMode(darkTheme) }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(themedContext)
            } else {
                dynamicLightColorScheme(themedContext)
            }
        }
        darkTheme -> darkColors(themedContext)
        else -> lightColors(themedContext)
    }
    CompositionLocalProvider(LocalThemedContext provides themedContext, LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

/**
 * Moko colors resolve through Android resources, which follow the system night mode, not the in-app theme.
 */
private fun Context.withNightMode(isDarkTheme: Boolean): Context {
    val nightMode = if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val currentConfiguration = resources.configuration
    if (currentConfiguration.uiMode and Configuration.UI_MODE_NIGHT_MASK == nightMode) return this
    val configuration = Configuration(currentConfiguration).apply {
        uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    }
    return createConfigurationContext(configuration)
}
