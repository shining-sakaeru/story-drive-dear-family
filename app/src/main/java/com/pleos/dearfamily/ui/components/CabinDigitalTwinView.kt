package com.pleos.dearfamily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.model.*
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

@Composable fun CabinDigitalTwinView(state: FamilyCareUiState, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "cabin").animateFloat(0f, 1f,
        infiniteRepeatable(tween(2300, easing = LinearEasing)), label = "heat rings")
    val cardBorder = CardBorder
    val emerald = Emerald
    val violet = Violet
    CarePanel(modifier) {
        PanelHeading("01", "우리 가족의 작은 우주", "CABIN DIGITAL TWIN · 6 SEATS")
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(378.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(Color(0xFF10161C), Offset(w * .045f, 0f), Size(w * .91f, h), CornerRadius(w * .24f, w * .24f))
                    drawRoundRect(cardBorder, Offset(w * .045f, 1f), Size(w * .91f, h - 2f), CornerRadius(w * .24f, w * .24f), style = Stroke(2.dp.toPx()))
                    drawRoundRect(emerald.copy(alpha = .10f), Offset(w * .20f, 13.dp.toPx()), Size(w * .60f, 17.dp.toPx()), CornerRadius(7.dp.toPx()))
                    state.seats.forEach { seat ->
                        val y = when (seat.zone) {
                            SeatZone.DRIVER_1L, SeatZone.PASSENGER_1R -> h * .24f
                            SeatZone.KIDS_2ND_ROW -> h * .54f
                            SeatZone.PARENTS_3RD_ROW -> h * .83f
                        }
                        val centers = when (seat.zone) {
                            SeatZone.DRIVER_1L -> listOf(w * .28f)
                            SeatZone.PASSENGER_1R -> listOf(w * .72f)
                            else -> listOf(w * .28f, w * .72f)
                        }
                        if (seat.active) centers.forEach { x ->
                            repeat(2) { ring ->
                                val progress = (pulse + ring * .5f) % 1f
                                drawCircle(Color(seat.zone.accent).copy(alpha = (1f - progress) * .36f),
                                    radius = 24.dp.toPx() + progress * 39.dp.toPx(), center = Offset(x, y), style = Stroke(1.5.dp.toPx()))
                            }
                        }
                    }
                }
                Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 42.dp),
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        SeatTile(state.seats.first { it.zone == SeatZone.DRIVER_1L }, "DRIVER", Modifier.weight(1f).testTag("seat_DRIVER_1L"))
                        SeatTile(state.seats.first { it.zone == SeatZone.PASSENGER_1R }, "PARTNER", Modifier.weight(1f).testTag("seat_PASSENGER_1R"))
                    }
                    PairSeats(state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }, "아이 01", "아이 02")
                    PairSeats(state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }, "어머니", "아버지")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("2열 마이크 · ${state.noiseDb} dB", color = Violet, style = MaterialTheme.typography.bodySmall)
                Text("기준 75 dB", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            }
            Canvas(Modifier.fillMaxWidth().height(25.dp)) {
                val p = Path()
                val amplitude = size.height * ((state.noiseDb - 30) / 70f).coerceIn(.06f, .48f)
                for (x in 0..160) {
                    val px = size.width * x / 160f
                    val py = size.height / 2 + sin(x * .24 + pulse * 2 * PI).toFloat() * amplitude * sin(x * .07).toFloat()
                    if (x == 0) p.moveTo(px, py) else p.lineTo(px, py)
                }
                drawPath(p, violet.copy(alpha = .8f), style = Stroke(1.6.dp.toPx()))
            }
            Text("온기와 소리는 가족에게만. 운전석은 평온하게.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun PairSeats(seat: SeatStatus, first: String, second: String) {
    Column(Modifier.testTag("seat_${seat.zone.name}")) {
        Text(seat.zone.label, color = Color(seat.zone.accent), style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SeatTile(seat, first, Modifier.weight(1f))
            SeatTile(seat, second, Modifier.weight(1f))
        }
    }
}

@Composable private fun SeatTile(seat: SeatStatus, label: String, modifier: Modifier) {
    val accent = Color(seat.zone.accent)
    Column(modifier.background(CardBackground.copy(alpha = .94f), RoundedCornerShape(12.dp))
        .border(1.dp, accent.copy(alpha = if (seat.active) .65f else .20f), RoundedCornerShape(12.dp))
        .padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text("${seat.temperature}°C  ·  ${seat.noiseDb}dB", style = MaterialTheme.typography.bodySmall)
        Text(when { seat.heatingLevel > 0 -> "온열 ${seat.heatingLevel} · 요추 ${if (seat.lumbarOn) "ON" else "OFF"}"
            !seat.ambientOn -> "조명 OFF · 수면 보호"
            seat.freshAir -> "외기 순환 · 독립 음장"
            else -> seat.status }, color = if (seat.active) accent else SecondaryText,
            style = MaterialTheme.typography.bodySmall)
    }
}
