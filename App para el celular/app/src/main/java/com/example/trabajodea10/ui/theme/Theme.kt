package com.example.trabajodea10.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.trabajodea10.data.TramaStore

private val EsquemaClaro = lightColorScheme(
    primary = NegroApple,
    onPrimary = BlancoPuro,
    primaryContainer = GrisChip,
    onPrimaryContainer = NegroApple,
    secondary = RojoAlerce,
    onSecondary = BlancoPuro,
    secondaryContainer = RojoAlerceContenedor,
    onSecondaryContainer = RojoAlerceOscuro,
    tertiary = NegroSuave,
    onTertiary = BlancoPuro,
    tertiaryContainer = GrisChip,
    onTertiaryContainer = NegroApple,
    error = RojoError,
    onError = BlancoPuro,
    errorContainer = RojoContenedor,
    onErrorContainer = RojoAlerceOscuro,
    background = FondoLimpio,
    onBackground = NegroApple,
    surface = SuperficieBlanca,
    onSurface = NegroApple,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    outline = LineaSuave,
    outlineVariant = LineaSuave,
    surfaceContainer = SuperficieBlanca,
    surfaceContainerLow = BlancoPuro,
    surfaceContainerHigh = Color(0xFFF4F4F5),
    surfaceContainerHighest = Color(0xFFE4E4E7)
)

private val EsquemaOscuro = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = BerenjenaOscuro,
    onPrimaryContainer = BerenjenaContenedor,
    secondary = Color(0xFF8FD6B8),
    onSecondary = Color(0xFF0E3A2A),
    secondaryContainer = Color(0xFF1E5540),
    onSecondaryContainer = VerdeContenedor,
    tertiary = Color(0xFFF5B96B),
    onTertiary = Color(0xFF452903),
    tertiaryContainer = Color(0xFF5D3B0B),
    onTertiaryContainer = NaranjaContenedor,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = DarkBackground,
    onBackground = Color(0xFFECE1E7),
    surface = DarkSurface,
    onSurface = Color(0xFFECE1E7),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

@Composable
fun TramaAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Modo accesible: si la persona toca "+A", multiplicamos la escala de fuente
    // que usa Android (fontScale). Así TODO el texto de la app se agranda de golpe
    // (incluso Material 3), sin tocar los tamaños en píxeles/dp del layout.
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale * TramaStore.escalaTexto
        )
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content
        )
    }
}
