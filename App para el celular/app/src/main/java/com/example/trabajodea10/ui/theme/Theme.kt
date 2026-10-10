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
    primary = Berenjena,
    onPrimary = Color.White,
    primaryContainer = BerenjenaContenedor,
    onPrimaryContainer = SobreBerenjenaContenedor,
    secondary = VerdeOk,
    onSecondary = Color.White,
    secondaryContainer = VerdeContenedor,
    onSecondaryContainer = Color(0xFF1C4A37),
    tertiary = NaranjaAlerta,
    onTertiary = Color.White,
    tertiaryContainer = NaranjaContenedor,
    onTertiaryContainer = Color(0xFF5A3208),
    error = RojoError,
    onError = Color.White,
    errorContainer = RojoContenedor,
    onErrorContainer = Color(0xFF6A1A12),
    background = FondoNeutro,
    onBackground = Color(0xFF221E22),
    surface = SuperficieBlanca,
    onSurface = Color(0xFF221E22),
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    outline = LineaSuave,
    outlineVariant = LineaSuave,
    surfaceContainer = SuperficieBlanca,
    surfaceContainerLow = Color(0xFFFCFAFC),
    surfaceContainerHigh = Color(0xFFF2EEF2),
    surfaceContainerHighest = Color(0xFFECE6EC)
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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
