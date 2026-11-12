package com.pleos.dearfamily.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeMode(val label: String, val subtitle: String) {
    GENESIS_LUXURY("모던 럭셔리", "Genesis ccNC Dark"),
    SCANDINAVIAN_WARM("웜 리빙룸", "Cozy Family Lounge"),
    SPATIAL_WIDGET("스페이셜 위젯", "CarPlay Dynamic")
}

data class ColorPalette(
    val background: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentTertiary: Color,
    val accentWarm: Color,
    val glassGlow: Brush
)

val LuxuryPalette = ColorPalette(
    background = Color(0xFF090D12),
    cardBackground = Color(0xFF131A22),
    cardBackgroundElevated = Color(0xFF1A232E),
    cardBorder = Color(0xFF263342),
    primaryText = Color(0xFFF2F6FA),
    secondaryText = Color(0xFF8898AA),
    accentPrimary = Color(0xFF00E5B8),    // Genesis Mint/Emerald
    accentSecondary = Color(0xFFFFB347),  // Warm Amber
    accentTertiary = Color(0xFF9B72FF),   // Violet
    accentWarm = Color(0xFFFF8A65),       // Coral
    glassGlow = Brush.verticalGradient(listOf(Color(0xFF00E5B8).copy(alpha = 0.08f), Color.Transparent))
)

val WarmLivingPalette = ColorPalette(
    background = Color(0xFF161210),
    cardBackground = Color(0xFF241C18),
    cardBackgroundElevated = Color(0xFF322722),
    cardBorder = Color(0xFF3E302A),
    primaryText = Color(0xFFFBF8F5),
    secondaryText = Color(0xFFB3A196),
    accentPrimary = Color(0xFFE5A96A),    // Honey Amber
    accentSecondary = Color(0xFF8FA876),  // Sage Green
    accentTertiary = Color(0xFFC792EA),   // Soft Lavender
    accentWarm = Color(0xFFE57B62),       // Terracotta
    glassGlow = Brush.verticalGradient(listOf(Color(0xFFE5A96A).copy(alpha = 0.09f), Color.Transparent))
)

val SpatialWidgetPalette = ColorPalette(
    background = Color(0xFF0C1017),
    cardBackground = Color(0xFF161C27),
    cardBackgroundElevated = Color(0xFF202A3B),
    cardBorder = Color(0xFF2B384E),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFF94A3B8),
    accentPrimary = Color(0xFF38BDF8),    // Electric Sky Blue
    accentSecondary = Color(0xFFFBBF24),  // Sun Gold
    accentTertiary = Color(0xFFA78BFA),   // Purple
    accentWarm = Color(0xFFFB7185),       // Rose
    glassGlow = Brush.verticalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.08f), Color.Transparent))
)

val LocalThemePalette = staticCompositionLocalOf { LuxuryPalette }
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.GENESIS_LUXURY }

// Backwards compatible colors for legacy components
val Background: Color @Composable get() = LocalThemePalette.current.background
val CardBackground: Color @Composable get() = LocalThemePalette.current.cardBackground
val CardBorder: Color @Composable get() = LocalThemePalette.current.cardBorder
val PrimaryText: Color @Composable get() = LocalThemePalette.current.primaryText
val SecondaryText: Color @Composable get() = LocalThemePalette.current.secondaryText
val Emerald: Color @Composable get() = LocalThemePalette.current.accentPrimary
val Amber: Color @Composable get() = LocalThemePalette.current.accentSecondary
val Violet: Color @Composable get() = LocalThemePalette.current.accentTertiary
val Coral: Color @Composable get() = LocalThemePalette.current.accentWarm

@Composable fun DearFamilyTheme(themeMode: ThemeMode = ThemeMode.GENESIS_LUXURY, content: @Composable () -> Unit) {
    val palette = when (themeMode) {
        ThemeMode.GENESIS_LUXURY -> LuxuryPalette
        ThemeMode.SCANDINAVIAN_WARM -> WarmLivingPalette
        ThemeMode.SPATIAL_WIDGET -> SpatialWidgetPalette
    }

    CompositionLocalProvider(
        LocalThemePalette provides palette,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = palette.accentPrimary,
                secondary = palette.accentSecondary,
                tertiary = palette.accentTertiary,
                background = palette.background,
                surface = palette.cardBackground,
                onSurface = palette.primaryText,
                onBackground = palette.primaryText,
                outline = palette.cardBorder
            ),
            typography = Typography(
                headlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
                headlineMedium = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
                headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
                titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
                titleSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
                bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, lineHeight = 25.sp),
                bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, lineHeight = 25.sp),
                bodySmall = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
                labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
                labelMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
                labelSmall = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
            ),
            content = content
        )
    }
}

@Composable fun CarePanel(
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalThemePalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = palette.cardBackground.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (highlight) palette.accentPrimary.copy(alpha = 0.6f) else palette.cardBorder)
    ) {
        Column(
            Modifier
                .background(if (highlight) palette.glassGlow else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable fun PanelHeading(index: String, title: String, subtitle: String, color: Color = Emerald) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                index,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.labelSmall)
        }
    }
}
