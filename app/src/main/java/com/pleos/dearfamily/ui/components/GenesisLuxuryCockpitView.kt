package com.pleos.dearfamily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatStatus
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GenesisLuxuryCockpitView(
    state: FamilyCareUiState,
    voice: VoiceStatus,
    onMute: () -> Unit,
    onToggleFun: () -> Unit,
    onSnack: (String) -> Unit,
    onEndConversation: () -> Unit,
    onRestStop: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // LEFT COLUMN: Driver Peace Shield & AI Telemetry
        // ==========================================
        CarePanel(
            modifier = Modifier.weight(0.31f).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(palette.accentPrimary)
                        )
                        Text(
                            "PEACE SHIELD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.accentPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "98% 무간섭",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Driver Shield Status Card
                val driverAudio = state.audio.first { it.zone == SeatZone.DRIVER_1L }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBackgroundElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seat_DRIVER_1L")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("1열 운전석 · 전방 주시 보호", fontWeight = FontWeight.Bold, color = palette.primaryText, style = MaterialTheme.typography.bodyMedium)
                        Text("후석 소음 방해 차단 · 내비 음성 우선 라우팅", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .semantics(mergeDescendants = true) {}
                                .testTag("audio_DRIVER_1L"),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("내비게이션 오디오", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                            Text("${driverAudio.volume}% · 전방 라우팅", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = palette.accentPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("음성 안내: " + voice.message, style = MaterialTheme.typography.labelSmall, color = palette.secondaryText, modifier = Modifier.testTag("ttsStatus"))
                            TextButton(onClick = onMute, modifier = Modifier.testTag("mute")) {
                                Text(if (voice.muted) "음소거 해제" else "음소거", color = palette.accentPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // AI Telemetry HUD (Displays real-time inference & satisfies assertMockIpoOutput)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBackgroundElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("실시간 AI 케어 파이프라인", fontWeight = FontWeight.Bold, color = palette.primaryText, style = MaterialTheme.typography.labelSmall)
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("센서 감지", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Text("차내 소음 ${state.noiseDb} dB · 주행 ${state.elapsedMin}분", style = MaterialTheme.typography.labelSmall, color = palette.primaryText, fontWeight = FontWeight.SemiBold)
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("AI 추론", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Text("${state.persona.badge} (신뢰도 96%)", style = MaterialTheme.typography.labelSmall, color = palette.accentSecondary, fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(color = palette.cardBorder, thickness = 0.5.dp)

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("차량 제어 출력", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = palette.secondaryText)
                            Text("Multi-Zone Adapter · MOCK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = palette.accentPrimary)
                            Text("• 2열 지향성 음장 및 공조 독립 제어\n• 운전석 간섭 0dB 보장 (시뮬레이션)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = palette.secondaryText)
                        }
                    }
                }
            }
        }

        // ==========================================
        // CENTER COLUMN: Genesis Cabin Digital Twin
        // ==========================================
        CarePanel(
            modifier = Modifier.weight(0.38f).fillMaxHeight(),
            highlight = true
        ) {
            val pulse by rememberInfiniteTransition(label = "cabin").animateFloat(
                0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "heat rings"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CABIN DIGITAL TWIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = palette.primaryText)
                    Text("6 SEATS · 4-ZONE", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                }

                // Interactive 3D Wireframe Car Silhouette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Vehicle Body Contour (Genesis ccNC Inspired Silhouette)
                        drawRoundRect(
                            color = Color(0xFF0F151C),
                            topLeft = Offset(w * 0.08f, 4.dp.toPx()),
                            size = Size(w * 0.84f, h - 8.dp.toPx()),
                            cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
                        )
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(palette.accentPrimary.copy(alpha = 0.6f), palette.cardBorder, palette.accentSecondary.copy(alpha = 0.4f))
                            ),
                            topLeft = Offset(w * 0.08f, 4.dp.toPx()),
                            size = Size(w * 0.84f, h - 8.dp.toPx()),
                            cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
                            style = Stroke(2.dp.toPx())
                        )

                        // Headlight Accents
                        drawRoundRect(
                            color = palette.accentPrimary.copy(alpha = 0.85f),
                            topLeft = Offset(w * 0.22f, 10.dp.toPx()),
                            size = Size(w * 0.56f, 6.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx())
                        )

                        // Seat Zone Halos & Glowing Circles
                        state.seats.forEach { seat ->
                            val y = when (seat.zone) {
                                SeatZone.DRIVER_1L, SeatZone.PASSENGER_1R -> h * 0.26f
                                SeatZone.KIDS_2ND_ROW -> h * 0.54f
                                SeatZone.PARENTS_3RD_ROW -> h * 0.80f
                            }
                            val centers = when (seat.zone) {
                                SeatZone.DRIVER_1L -> listOf(w * 0.32f)
                                SeatZone.PASSENGER_1R -> listOf(w * 0.68f)
                                else -> listOf(w * 0.32f, w * 0.68f)
                            }
                            val accent = Color(seat.zone.accent)

                            centers.forEach { x ->
                                if (seat.active) {
                                    val r = (pulse * 30.dp.toPx())
                                    drawCircle(accent.copy(alpha = (1f - pulse) * 0.4f), radius = 18.dp.toPx() + r, center = Offset(x, y))
                                }
                                drawCircle(
                                    color = if (seat.active) accent else palette.cardBorder,
                                    radius = 16.dp.toPx(),
                                    center = Offset(x, y),
                                    style = Stroke(2.dp.toPx())
                                )
                                drawCircle(
                                    color = if (seat.active) accent.copy(alpha = 0.3f) else Color(0xFF161E28),
                                    radius = 14.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }

                // 2nd & 3rd Row Luxury Seat Status Badges (Required for test assertions)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 2nd Row Kids/Teens
                    val kidsSeat = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("seat_KIDS_2ND_ROW"),
                        shape = RoundedCornerShape(12.dp),
                        color = palette.cardBackgroundElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(SeatZone.KIDS_2ND_ROW.accent).copy(alpha = if (kidsSeat.active) 0.8f else 0.2f))
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("2열 ${state.persona.title}", color = Color(SeatZone.KIDS_2ND_ROW.accent), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("${kidsSeat.temperature}°C · ${state.noiseDb} dB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    !kidsSeat.ambientOn -> "조명 OFF · 수면 보호"
                                    kidsSeat.freshAir -> "외기 순환 · 독립 음장"
                                    kidsSeat.active -> "외기 순환 · 케어 활성"
                                    else -> kidsSeat.status
                                },
                                color = if (kidsSeat.active) Color(SeatZone.KIDS_2ND_ROW.accent) else palette.secondaryText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // 3rd Row Parents
                    val parentsSeat = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("seat_PARENTS_3RD_ROW"),
                        shape = RoundedCornerShape(12.dp),
                        color = palette.cardBackgroundElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(SeatZone.PARENTS_3RD_ROW.accent).copy(alpha = if (parentsSeat.active) 0.8f else 0.2f))
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("3열 부모님", color = Color(SeatZone.PARENTS_3RD_ROW.accent), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("${parentsSeat.temperature}°C · ${state.elapsedMin}분", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    parentsSeat.heatingLevel > 0 -> "온열 ${parentsSeat.heatingLevel}단 · 요추 ${if (parentsSeat.lumbarOn) "ON" else "OFF"}"
                                    parentsSeat.active -> "피로 완화 케어 중"
                                    else -> parentsSeat.status
                                },
                                color = if (parentsSeat.active) Color(SeatZone.PARENTS_3RD_ROW.accent) else palette.secondaryText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Acoustic Waveform
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("차내 어쿠스틱 마이크", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                        Text("${state.noiseDb} dB (기준 75dB)", style = MaterialTheme.typography.labelSmall, color = palette.accentTertiary, fontWeight = FontWeight.Bold)
                    }
                    Canvas(Modifier.fillMaxWidth().height(20.dp)) {
                        val p = Path()
                        val amp = size.height * ((state.noiseDb - 30) / 70f).coerceIn(0.1f, 0.5f)
                        for (x in 0..120) {
                            val px = size.width * x / 120f
                            val py = size.height / 2 + sin(x * 0.25 + pulse * 2 * PI).toFloat() * amp * sin(x * 0.08).toFloat()
                            if (x == 0) p.moveTo(px, py) else p.lineTo(px, py)
                        }
                        drawPath(p, Color(SeatZone.KIDS_2ND_ROW.accent).copy(alpha = 0.85f), style = Stroke(1.8.dp.toPx()))
                    }
                }
            }
        }

        // ==========================================
        // RIGHT COLUMN: Active Family Care & Multi-Zone Media
        // ==========================================
        CarePanel(
            modifier = Modifier.weight(0.31f).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Scenario Story Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBackgroundElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentTertiary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("현재 케어 시나리오", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = palette.accentTertiary.copy(alpha = 0.2f)
                            ) {
                                Text("ACTIVE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.accentTertiary, modifier = Modifier.padding(4.dp, 1.dp))
                            }
                        }

                        Text(
                            state.flow.activeScenarioTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.primaryText,
                            modifier = Modifier.testTag("activeScenario")
                        )

                        Text(
                            state.flow.gleoTtsScript.ifBlank {
                                "온 가족이 각자의 자리에서 가장 편안한 상태를 누릴 수 있도록 AI가 보이지 않는 동반자로 함께합니다."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.primaryText.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )

                        // 2nd Row Audio Status (contains SIMULATION)
                        val kidsAudio = state.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .semantics(mergeDescendants = true) {}
                                .testTag("audio_KIDS_2ND_ROW"),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("2열 오디오북", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Text("${kidsAudio.volume}% · ${kidsAudio.status}", style = MaterialTheme.typography.labelSmall, color = palette.accentTertiary)
                        }
                    }
                }

                // Passenger Cozy Lounge Card (With funToggle, snackWalnut, snackChoice, conversationEnd)
                val passengerSeat = state.seats.first { it.zone == SeatZone.PASSENGER_1R }
                val passengerAudio = state.audio.first { it.zone == SeatZone.PASSENGER_1R }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardBackgroundElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentSecondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seat_PASSENGER_1R")
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("1열 조수석 · ${passengerSeat.status}", fontWeight = FontWeight.Bold, color = palette.accentSecondary, style = MaterialTheme.typography.labelSmall)
                            TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle")) {
                                Text(if (state.cozyMode) "Fun 모드" else "Cozy 모드", color = palette.accentSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (!state.cozyMode) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("우리 부부 이구동성 밸런스 게임", color = palette.accentSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onSnack("호두과자") }, modifier = Modifier.testTag("snackWalnut")) {
                                        Text("호두과자", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(onClick = { onSnack("알감자") }) {
                                        Text("알감자", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                state.snackChoice?.let {
                                    Text("선택: $it", color = palette.accentSecondary, modifier = Modifier.testTag("snackChoice"), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            Text("따뜻한 앰버 조명 및 대화 시 25% 음악 자동 덕킹", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                        }

                        OutlinedButton(
                            onClick = onEndConversation,
                            modifier = Modifier.fillMaxWidth().testTag("conversationEnd")
                        ) {
                            Text("대화 종료 · 음악 복원", style = MaterialTheme.typography.labelSmall)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {}
                                .testTag("audio_PASSENGER_1R"),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("조수석 미디어", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Text("${passengerAudio.volume}% · ${passengerAudio.status}", style = MaterialTheme.typography.labelSmall, color = palette.accentSecondary)
                        }
                    }
                }

                // 3rd Row Parents Rest Stop Proposal
                val parentsAudio = state.audio.first { it.zone == SeatZone.PARENTS_3RD_ROW }
                if (state.restStopVisible) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.cardBackgroundElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentWarm.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("부모님 휴식을 위해 평창휴게소를 경유할까요?", color = palette.accentWarm, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("예상 시간 10분 소요 · 모의 경유 추가", color = palette.secondaryText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))

                            if (state.restStopAccepted == null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onRestStop(true) }, modifier = Modifier.testTag("restAccept")) {
                                        Text("모의 경유 추가", style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = { onRestStop(false) }) {
                                        Text("통과", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            } else {
                                Text(
                                    if (state.restStopAccepted == true) "모의 경유가 추가되었습니다." else "기존 경로 유지",
                                    color = palette.accentWarm,
                                    modifier = Modifier.testTag("restAnswer"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                        .testTag("audio_PARENTS_3RD_ROW"),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3열 오디오", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                    Text("${parentsAudio.volume}% · ${parentsAudio.status}", style = MaterialTheme.typography.labelSmall, color = palette.accentWarm)
                }
            }
        }
    }
}
