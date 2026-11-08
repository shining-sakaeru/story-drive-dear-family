package com.pleos.dearfamily.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Background = Color(0xFF0F1216)
val CardBackground = Color(0xFF1A2028)
val CardBorder = Color(0xFF2D3644)
val PrimaryText = Color(0xFFF5F7FA)
val SecondaryText = Color(0xFF9BA5B4)
val Emerald = Color(0xFF00E5B8)
val Amber = Color(0xFFFFB347)
val Violet = Color(0xFF9B72FF)
val Coral = Color(0xFFFF8A65)

@Composable fun DearFamilyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Emerald, secondary = Amber, tertiary = Violet,
            background = Background, surface = CardBackground, onSurface = PrimaryText,
            onBackground = PrimaryText, outline = CardBorder),
        typography = Typography(
            headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
            bodySmall = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, lineHeight = 16.sp),
            labelSmall = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, letterSpacing = 1.sp)
        ), content = content
    )
}

@Composable fun CarePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp),
        color = CardBackground.copy(alpha = .85f), border = BorderStroke(1.dp, CardBorder)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable fun PanelHeading(index: String, title: String, subtitle: String, color: Color = Emerald) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(index, color = color, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.labelSmall)
        }
    }
}
