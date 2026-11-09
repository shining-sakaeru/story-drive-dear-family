package com.pleos.dearfamily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * [Option 3: Spatial Dynamic Cards / Next-Gen CarPlay & Android Automotive Widget Style]
 * Faithfully reproduces ui_mockup_option3_spatial_dynamic_widgets.jpg:
 * - High-contrast modern dark mode with 4 distinct vibrant neon-bordered quadrant cards:
 *   1. 'Driver Peace Shield' (Electric Cyan #00E5FF border, ANC 85%, Driver Fatigue Alert: Good, quick mute)
 *   2. 'Co-Pilot Lounge' (Coral Orange #FF6B4A border, album cover art, seat ventilation, balance game)
 *   3. '2nd Row Kids Dreamland' (Lavender Violet #B388FF border, bedtime story 'Magic Galaxy', dynamic neon audio waveform visualizer)
 *   4. '3rd Row Comfort' (Warm Gold #FFD54F border, heated seat level 1, air purification Pure 8 AQI)
 * - Bottom Dock: Sleek floating translucent pill dock containing quick vehicle controls and scenario pill
 */
@Composable
fun SpatialDynamicCardsView(
    state: FamilyCareUiState,
    voice: VoiceStatus,
    onMute: () -> Unit,
    onToggleFun: () -> Unit,
    onSnack: (String) -> Unit,
    onEndConversation: () -> Unit,
    onRestStop: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spatialWaves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "spatialWaves"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP ROW: Quadrant 1 (Driver Peace Shield) & Quadrant 2 (Co-Pilot Lounge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [Quadrant 1: Driver Peace Shield (Electric Cyan Border)]
            val driverAudio = state.audio.first { it.zone == SeatZone.DRIVER_1L }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_DRIVER_1L"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF101520),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF))
                            )
                            Text(
                                "DRIVER PEACE SHIELD",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "ANC 85%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                                .border(2.dp, Color(0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("전방 주시 보호 · 피로도 양호", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("후석 3개 존 소음 간섭 차단 · 내비 음성 우선 라우팅", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFF94A3B8))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .testTag("audio_DRIVER_1L"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("내비게이션 오디오", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text("${driverAudio.volume}% · 전방 라우팅", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "음성 안내 · " + voice.message,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f).testTag("ttsStatus")
                        )
                        TextButton(onClick = onMute, modifier = Modifier.testTag("mute")) {
                            Text(if (voice.muted) "음소거 해제" else "음소거", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // [Quadrant 2: Co-Pilot Lounge (Coral Orange Border)]
            val passengerSeat = state.seats.first { it.zone == SeatZone.PASSENGER_1R }
            val passengerAudio = state.audio.first { it.zone == SeatZone.PASSENGER_1R }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_PASSENGER_1R"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF181313),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF6B4A)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6B4A))
                            )
                            Text(
                                "CO-PILOT LOUNGE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B4A),
                                letterSpacing = 0.5.sp
                            )
                        }
                        TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle")) {
                            Text(if (state.cozyMode) "게임 켜기" else "대화 복원", color = Color(0xFFFF6B4A), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Album Art & Lounge Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini Album Art
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF6B4A), Color(0xFFFF9E7D), Color(0xFFB388FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎶", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Sunset Highway Lo-Fi", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("1열 조수석 · ${passengerSeat.status}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFFF6B4A), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (!state.cozyMode) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onSnack("호두과자") },
                                modifier = Modifier.testTag("snackWalnut"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B4A), contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("호두과자", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = onEndConversation,
                                modifier = Modifier.testTag("conversationEnd"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("대화 종료", style = MaterialTheme.typography.labelSmall)
                            }
                            state.snackChoice?.let {
                                Text("선택: $it", color = Color(0xFFFF6B4A), modifier = Modifier.testTag("snackChoice"), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Text("부드러운 Lo-Fi 앰비언트 사운드 재생 중 · 대화 시 25% 자동 덕킹", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFF94A3B8))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .testTag("audio_PASSENGER_1R"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("조수석 미디어", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text("${passengerAudio.volume}% · ${passengerAudio.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B4A))
                    }
                }
            }
        }

        // BOTTOM ROW: Quadrant 3 (Kids Dreamland) & Quadrant 4 (3rd Row Comfort)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [Quadrant 3: 2nd Row Kids Dreamland (Lavender Violet Border)]
            val kidsSeat = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
            val kidsAudio = state.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_KIDS_2ND_ROW"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF14111E),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB388FF)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB388FF))
                            )
                            Text(
                                "2ND ROW KIDS DREAMLAND",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB388FF),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFB388FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "2열 ${state.persona.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB388FF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Scenario Title & Status Text
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "⭐ " + state.flow.activeScenarioTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("activeScenario")
                        )
                        val statusText = when {
                            !kidsSeat.ambientOn -> "조명 OFF · 수면 보호 · 귓속말 동화"
                            kidsSeat.freshAir -> "외기 순환 · 독립 음장 제어"
                            kidsSeat.active -> "외기 순환 · 매직 위스퍼 활성"
                            else -> kidsSeat.status
                        }
                        Text(statusText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB388FF), fontWeight = FontWeight.Bold)
                    }

                    // Dynamic Neon Audio Waveform Visualizer
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        val barCount = 24
                        val barWidth = size.width / (barCount * 1.5f)
                        for (i in 0 until barCount) {
                            val factor = sin((i.toFloat() / barCount * 2 * PI) + waveOffset).toFloat().coerceIn(0.15f, 1f)
                            val barHeight = size.height * factor
                            val left = i * (barWidth * 1.5f)
                            val top = size.height - barHeight
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFFB388FF))
                                ),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .testTag("audio_KIDS_2ND_ROW"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("2열 오디오북", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text("${kidsAudio.volume}% · ${kidsAudio.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFB388FF))
                    }
                }
            }

            // [Quadrant 4: 3rd Row Comfort (Warm Gold Border)]
            val parentsSeat = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
            val parentsAudio = state.audio.first { it.zone == SeatZone.PARENTS_3RD_ROW }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_PARENTS_3RD_ROW"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF161510),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD54F))
                            )
                            Text(
                                "3RD ROW COMFORT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Pure 8 AQI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Status text (must contain "온열 1" for test assertion)
                    val parentsText = when {
                        parentsSeat.heatingLevel > 0 -> "온열 ${parentsSeat.heatingLevel}단 · 요추 ${if (parentsSeat.lumbarOn) "ON" else "OFF"}"
                        parentsSeat.active -> "온열 1단 · 피로 케어 활성"
                        else -> parentsSeat.status
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("3열 부모님 케어", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text(parentsText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }

                    // Rest stop proposal
                    if (state.restStopVisible) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                        ) {
                            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("평창휴게소를 경유할까요?", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                if (state.restStopAccepted == null) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { onRestStop(true) },
                                            modifier = Modifier.testTag("restAccept"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("모의 경유 추가", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                        }
                                        TextButton(onClick = { onRestStop(false) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text("통과", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                        }
                                    }
                                } else {
                                    Text(
                                        if (state.restStopAccepted == true) "모의 경유가 추가되었습니다." else "기존 경로 유지",
                                        color = Color(0xFFFFD54F),
                                        modifier = Modifier.testTag("restAnswer"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Text("온열 1단 및 요추 지압 작동 중 · 편안한 휴식", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFF94A3B8))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .testTag("audio_PARENTS_3RD_ROW"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("3열 오디오", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text("${parentsAudio.volume}% · ${parentsAudio.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    }
                }
            }
        }

        // Floating Bottom Translucent Pill Dock (Matching Mockup Dock: Home | HVAC | Media | Nav | Scenario)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xDD111622),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠 홈", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("❄️ 공조", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text("🎶 미디어", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text("🧭 내비", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFB388FF).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB388FF))
                ) {
                    Text(
                        "⚡ 시나리오: Night Journey",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB388FF),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
