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
import kotlin.math.cos
import kotlin.math.sin

/**
 * [Option 2: Warm Emotional Family Care / Scandinavian Living Room Style]
 * Faithfully reproduces ui_mockup_option2_scandinavian_family_care.jpg:
 * - Aesthetic: Warm espresso, dark taupe matte backgrounds, soft amber candlelight backlights, muted sage green accents
 * - 3 Large Friendly Emotional Family Cards (Horizontal Triad):
 *   1) Left Card: 'Kids Dream Story' showing charming storybook illustration of sleeping fox under starlight
 *   2) Center Card: 'Family Harmony' showing serene cabin status 'Peaceful & Serene · 98% Harmony', warm cabin illustration
 *   3) Right Card: 'Driver & Co-Pilot Rest' showing cozy seat comfort, gentle lumbar massage active, soft lo-fi background
 * - Bottom Dock: Large soft pill buttons for 'Quiet Cabin', 'Play Story', 'Cabin Warmth'
 */
@Composable
fun WarmFamilyStoryView(
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

    val infiniteTransition = rememberInfiniteTransition(label = "warmPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "warmPulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 3 Large Friendly Emotional Family Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // CARD 1 (LEFT): Kids Dream Story (Sleeping Fox under Starlight Storybook)
            // =========================================================================
            val kidsSeat = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
            val kidsAudio = state.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }
            Surface(
                modifier = Modifier
                    .weight(0.33f)
                    .fillMaxHeight()
                    .testTag("seat_KIDS_2ND_ROW"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF221B17),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE28B59).copy(alpha = 0.5f)),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE28B59))
                            )
                            Text(
                                "KIDS DREAM STORY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE28B59),
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE28B59).copy(alpha = 0.18f)
                        ) {
                            Text(
                                "Ch. 3 별빛 여행",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE28B59),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Charming Storybook Canvas Illustration: Sleeping Fox Under Starlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Midnight gradient sky
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF151B28), Color(0xFF1F2432), Color(0xFF1A1A24))
                                ),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )

                            // Crescent Moon
                            val moonCenter = Offset(w * 0.82f, h * 0.28f)
                            drawCircle(color = Color(0xFFFDE68A), radius = 18.dp.toPx(), center = moonCenter)
                            drawCircle(color = Color(0xFF171D2B), radius = 15.dp.toPx(), center = Offset(moonCenter.x - 6.dp.toPx(), moonCenter.y - 4.dp.toPx()))

                            // Twinkling Stars
                            val starPoints = listOf(
                                Offset(w * 0.15f, h * 0.20f), Offset(w * 0.35f, h * 0.15f),
                                Offset(w * 0.55f, h * 0.25f), Offset(w * 0.70f, h * 0.18f),
                                Offset(w * 0.25f, h * 0.40f), Offset(w * 0.45f, h * 0.45f)
                            )
                            starPoints.forEachIndexed { idx, pt ->
                                val starAlpha = 0.4f + 0.6f * sin((pulse * 2 * PI + idx).toFloat()).coerceIn(0f, 1f)
                                drawCircle(color = Color.White.copy(alpha = starAlpha), radius = 2.dp.toPx(), center = pt)
                            }

                            // Mossy Hill Mound
                            val mound = Path().apply {
                                moveTo(0f, h)
                                cubicTo(w * 0.3f, h * 0.72f, w * 0.7f, h * 0.75f, w, h * 0.85f)
                                lineTo(w, h)
                                close()
                            }
                            drawPath(mound, Brush.verticalGradient(listOf(Color(0xFF283828), Color(0xFF1A261A))))

                            // Sleeping Fox Body
                            val foxX = w * 0.48f
                            val foxY = h * 0.78f
                            // Body circle
                            drawCircle(color = Color(0xFFE28B59), radius = 22.dp.toPx(), center = Offset(foxX, foxY))
                            // Fluffy Tail curled
                            drawCircle(color = Color(0xFFC86F3E), radius = 16.dp.toPx(), center = Offset(foxX + 16.dp.toPx(), foxY + 2.dp.toPx()))
                            drawCircle(color = Color(0xFFFFF7ED), radius = 8.dp.toPx(), center = Offset(foxX + 26.dp.toPx(), foxY))
                            // Head
                            drawCircle(color = Color(0xFFE28B59), radius = 14.dp.toPx(), center = Offset(foxX - 14.dp.toPx(), foxY - 4.dp.toPx()))
                            // White cheeks
                            drawCircle(color = Color(0xFFFFF7ED), radius = 7.dp.toPx(), center = Offset(foxX - 16.dp.toPx(), foxY))
                            // Closed sleeping eye
                            drawLine(
                                color = Color(0xFF2E1A11),
                                start = Offset(foxX - 19.dp.toPx(), foxY - 3.dp.toPx()),
                                end = Offset(foxX - 13.dp.toPx(), foxY - 3.dp.toPx()),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Ears
                            val ear1 = Path().apply {
                                moveTo(foxX - 22.dp.toPx(), foxY - 14.dp.toPx())
                                lineTo(foxX - 16.dp.toPx(), foxY - 26.dp.toPx())
                                lineTo(foxX - 12.dp.toPx(), foxY - 14.dp.toPx())
                                close()
                            }
                            drawPath(ear1, Color(0xFFE28B59))
                        }
                    }

                    // Active Scenario Story Text
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "⭐ " + state.flow.activeScenarioTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.testTag("activeScenario")
                        )
                        Text(
                            state.flow.gleoTtsScript.ifBlank {
                                "쉿! 잠든 거인의 비밀 터널에 들어왔어. 가장 작은 목소리로 구름 사탕이라고 속삭여 볼까?"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = Color(0xFFF5EBE1),
                            maxLines = 2
                        )
                    }

                    // Seat Status (Required for test assertions: "외기 순환" when noise, "조명 OFF" when sleep)
                    val statusText = when {
                        !kidsSeat.ambientOn -> "조명 OFF · 꿀잠 수면 보호"
                        kidsSeat.freshAir -> "외기 순환 · 독립 음장 제어"
                        kidsSeat.active -> "외기 순환 · 따뜻한 동화 활성"
                        else -> kidsSeat.status
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2D231E)
                    ) {
                        Text(
                            "상태: $statusText",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE28B59),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Volume Bar & Audio Status (contains SIMULATION)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .testTag("audio_KIDS_2ND_ROW"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2열 오디오북", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB5A496))
                        Text("${kidsAudio.volume}% · ${kidsAudio.status}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, color = Color(0xFFE28B59))
                    }
                }
            }

            // =========================================================================
            // CARD 2 (CENTER): Family Harmony (Cozy Winter Cabin & 98% Harmony Gauge)
            // =========================================================================
            val driverAudio = state.audio.first { it.zone == SeatZone.DRIVER_1L }
            Surface(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF221B17),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Text(
                                "FAMILY HARMONY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                        ) {
                            Text(
                                "98% 화목",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Cozy Winter Cabin Canvas Illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Twilight warm mountain sky
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF2A1C16), Color(0xFF3B271F), Color(0xFF221712))
                                ),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )

                            // Snowy mountain silhouette
                            val mountain = Path().apply {
                                moveTo(0f, h * 0.7f)
                                lineTo(w * 0.35f, h * 0.25f)
                                lineTo(w * 0.7f, h * 0.65f)
                                lineTo(w, h * 0.4f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(mountain, Color(0xFF2F211A))

                            // Snow-covered ground
                            val ground = Path().apply {
                                moveTo(0f, h * 0.8f)
                                cubicTo(w * 0.4f, h * 0.75f, w * 0.7f, h * 0.85f, w, h * 0.78f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(ground, Color(0xFFEDE0D4))

                            // Pine Trees on sides
                            for (treeX in listOf(w * 0.12f, w * 0.22f, w * 0.85f)) {
                                val tree = Path().apply {
                                    moveTo(treeX, h * 0.45f)
                                    lineTo(treeX + 12.dp.toPx(), h * 0.75f)
                                    lineTo(treeX - 12.dp.toPx(), h * 0.75f)
                                    close()
                                }
                                drawPath(tree, Color(0xFF1E2D1E))
                            }

                            // Wooden Cabin A-Frame
                            val cabinX = w * 0.52f
                            val cabinY = h * 0.55f
                            val cabinW = 60.dp.toPx()
                            val cabinH = 45.dp.toPx()

                            // Cabin Walls
                            drawRect(
                                color = Color(0xFF6B4226),
                                topLeft = Offset(cabinX - cabinW / 2, cabinY),
                                size = Size(cabinW, cabinH)
                            )
                            // A-Frame Roof
                            val roof = Path().apply {
                                moveTo(cabinX, cabinY - 24.dp.toPx())
                                lineTo(cabinX + cabinW / 2 + 8.dp.toPx(), cabinY + 2.dp.toPx())
                                lineTo(cabinX - cabinW / 2 - 8.dp.toPx(), cabinY + 2.dp.toPx())
                                close()
                            }
                            drawPath(roof, Color(0xFFFDFBF7)) // Snow on roof

                            // Smoking Chimney
                            drawRect(
                                color = Color(0xFF52321D),
                                topLeft = Offset(cabinX + 12.dp.toPx(), cabinY - 28.dp.toPx()),
                                size = Size(8.dp.toPx(), 18.dp.toPx())
                            )
                            // Smoke puffs
                            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 5.dp.toPx(), center = Offset(cabinX + 16.dp.toPx(), cabinY - 34.dp.toPx()))
                            drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 7.dp.toPx(), center = Offset(cabinX + 20.dp.toPx(), cabinY - 44.dp.toPx()))

                            // Glowing Amber Windows
                            val windowGlow = Color(0xFFFBBF24)
                            drawRoundRect(
                                color = windowGlow,
                                topLeft = Offset(cabinX - 18.dp.toPx(), cabinY + 12.dp.toPx()),
                                size = Size(14.dp.toPx(), 14.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = windowGlow,
                                topLeft = Offset(cabinX + 4.dp.toPx(), cabinY + 12.dp.toPx()),
                                size = Size(14.dp.toPx(), 14.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }

                    // Harmony summary & Driver Peace Shield (tag: seat_DRIVER_1L)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seat_DRIVER_1L"),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2D231E)
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("운전석 피스 실드", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall)
                                Text("100% 무간섭", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {}
                                    .testTag("audio_DRIVER_1L"),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("내비게이션 라우팅", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB5A496))
                                Text("${driverAudio.volume}% · 전방 라우팅", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("음성: " + voice.message, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFB5A496), modifier = Modifier.weight(1f).testTag("ttsStatus"))
                                TextButton(onClick = onMute, modifier = Modifier.testTag("mute"), contentPadding = PaddingValues(0.dp)) {
                                    Text(if (voice.muted) "소리 켬" else "음소거", color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }
                            }
                        }
                    }

                    // Multi-Zone Adapter info
                    Text("Multi-Zone Adapter · MOCK (모의 안전 조율)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFB5A496))
                }
            }

            // =========================================================================
            // CARD 3 (RIGHT): Driver & Co-Pilot Rest (Ergonomic Seats & Parents Comfort)
            // =========================================================================
            val passengerSeat = state.seats.first { it.zone == SeatZone.PASSENGER_1R }
            val passengerAudio = state.audio.first { it.zone == SeatZone.PASSENGER_1R }
            val parentsSeat = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
            val parentsAudio = state.audio.first { it.zone == SeatZone.PARENTS_3RD_ROW }

            Surface(
                modifier = Modifier
                    .weight(0.33f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF221B17),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                "COZY REST & LOUNGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                letterSpacing = 1.sp
                            )
                        }
                        TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle"), contentPadding = PaddingValues(0.dp)) {
                            Text(if (state.cozyMode) "Fun 모드" else "Cozy 모드", color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Co-Pilot Lounge Card (tag: seat_PASSENGER_1R)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seat_PASSENGER_1R"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2D231E)
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("1열 조수석 · ${passengerSeat.status}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)

                            if (!state.cozyMode) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { onSnack("호두과자") },
                                        modifier = Modifier.testTag("snackWalnut"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("호두과자", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                    }
                                    OutlinedButton(
                                        onClick = { onSnack("알감자") },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("알감자", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                    }
                                }
                                state.snackChoice?.let {
                                    Text("선택: $it", color = Color(0xFF10B981), modifier = Modifier.testTag("snackChoice"), style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Text("잔잔한 Lo-Fi 배경음 · 대화 시 25% 자동 덕킹", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB5A496))
                            }

                            OutlinedButton(
                                onClick = onEndConversation,
                                modifier = Modifier.fillMaxWidth().testTag("conversationEnd"),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                Text("대화 종료 · 음악 복원", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {}
                                    .testTag("audio_PASSENGER_1R"),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("조수석 미디어", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB5A496))
                                Text("${passengerAudio.volume}% · ${passengerAudio.status}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    // 3rd Row Parents Care (tag: seat_PARENTS_3RD_ROW)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seat_PARENTS_3RD_ROW"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2D231E)
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val parentsText = when {
                                parentsSeat.heatingLevel > 0 -> "온열 ${parentsSeat.heatingLevel}단 · 요추 ${if (parentsSeat.lumbarOn) "ON" else "OFF"}"
                                parentsSeat.active -> "온열 1단 · 피로 케어"
                                else -> parentsSeat.status
                            }
                            Text("3열 부모님 · $parentsText", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall)

                            if (state.restStopVisible) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("평창휴게소를 경유할까요?", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                    if (state.restStopAccepted == null) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { onRestStop(true) },
                                                modifier = Modifier.testTag("restAccept"),
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
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.testTag("restAnswer"),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold
                                        )
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
                                Text("3열 오디오", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFB5A496))
                                Text("${parentsAudio.volume}% · ${parentsAudio.status}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Soft Tactile Action Pills (Matching Mockup Dock: Quiet Cabin | Play Story | Cabin Warmth)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF221B17),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382C24)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2D231E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE28B59).copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onMute() }
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎤", fontSize = 14.sp)
                        Text("조용한 차내 (Quiet Cabin)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF5EBE1))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE28B59),
                    modifier = Modifier.clickable { onToggleFun() }
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("▶", fontSize = 14.sp, color = Color.Black)
                        Text("동화 재생 (Play Story)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2D231E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🌡", fontSize = 14.sp)
                        Text("포근한 온도 22.5°C (Cabin Warmth)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF5EBE1))
                    }
                }
            }
        }
    }
}
