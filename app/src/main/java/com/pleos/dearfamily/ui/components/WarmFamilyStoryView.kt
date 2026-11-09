package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.*

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

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Family Storybook Card & Ambient Narrative
        CarePanel(
            modifier = Modifier.weight(0.48f).fillMaxHeight(),
            highlight = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(palette.accentPrimary)
                    )
                    Text(
                        "가족의 동화 이야기",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.accentPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.accentPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        if (state.tourRunning) "실시간 탐험 중" else "평온 휴식 모드",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accentPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Story Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = palette.cardBackgroundElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "⭐ " + state.flow.activeScenarioTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.primaryText,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("activeScenario")
                        )

                        Text(
                            state.flow.gleoTtsScript.ifBlank {
                                "우리 가족만을 위한 보이지 않는 AI 동반자가 함께하고 있습니다. 탑승객 모두가 평온한 시간을 누리세요."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.primaryText.copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "음성 상태 · " + voice.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText,
                                modifier = Modifier.testTag("ttsStatus")
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = onMute, modifier = Modifier.testTag("mute")) {
                                Text(if (voice.muted) "소리 켜기" else "음소거", color = palette.accentPrimary)
                            }
                        }
                    }
                }

                // Family Harmony Summary
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = palette.cardBackgroundElevated.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("차내 평온 지수", style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                            Text("99% 완벽한 휴식", style = MaterialTheme.typography.titleLarge, color = palette.accentPrimary)
                            Text("모든 좌석이 편안하게 조율되었습니다", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                        }
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(palette.accentPrimary.copy(alpha = 0.15f))
                                .border(2.dp, palette.accentPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♥", fontSize = 24.sp, color = palette.accentPrimary)
                        }
                    }
                }

                // Rest Stop proposal if active
                if (state.restStopVisible) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.accentSecondary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("☕ 부모님을 위한 따뜻한 휴게소 추천", fontWeight = FontWeight.Bold, color = palette.accentSecondary)
                            Text("장거리 이동으로 뻐근하실 부모님을 위해 횡성휴게소 15분 경유를 추천합니다.", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onRestStop(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentSecondary, contentColor = Color.Black),
                                    modifier = Modifier.testTag("restAccept")
                                ) {
                                    Text("경유지 추가")
                                }
                                OutlinedButton(onClick = { onRestStop(false) }) {
                                    Text("통과")
                                }
                            }
                            state.restStopAccepted?.let {
                                Text(if (it) "횡성휴게소(15분) 모의 경유 등록됨" else "휴게소 통과", color = palette.accentSecondary, modifier = Modifier.testTag("restAnswer"))
                            }
                        }
                    }
                }
            }
        }

        // Right Column: 4 Seats Lounge Cards & Zone Controls
        CarePanel(
            modifier = Modifier.weight(0.52f).fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "가족 좌석별 맞춤 케어",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle")) {
                    Text(if (state.cozyMode) "게임 모드" else "휴식 모드", color = palette.accentSecondary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.seats.forEach { seat ->
                    val zoneColor = Color(seat.zone.accent)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.cardBackgroundElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (seat.active) zoneColor.copy(alpha = 0.6f) else palette.cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seat_${seat.zone.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(zoneColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (seat.zone) {
                                        SeatZone.DRIVER_1L -> "운전"
                                        SeatZone.PASSENGER_1R -> "조수"
                                        SeatZone.KIDS_2ND_ROW -> "아이"
                                        SeatZone.PARENTS_3RD_ROW -> "부모"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = zoneColor
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(seat.zone.label, fontWeight = FontWeight.Bold, color = zoneColor)
                                    Text("${seat.temperature}°C · ${seat.noiseDb}dB", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                                }
                                Text(
                                    when {
                                        seat.heatingLevel > 0 -> "등받이 온열 ${seat.heatingLevel}단 · 요추 케어 작동 중"
                                        !seat.ambientOn -> "조명 OFF · 꿀잠 수면 보호 중"
                                        seat.freshAir -> "외기 순환 · 상단 산뜻한 미풍 · 독립 음장"
                                        else -> seat.status
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (seat.active) zoneColor else palette.secondaryText
                                )
                            }
                        }
                    }
                }

                // Audio Status Rows for tests
                state.audio.forEach { zone ->
                    val zColor = Color(zone.zone.accent)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("audio_${zone.zone.name}"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(zone.zone.label, style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                        Text("${zone.volume}% · ${zone.status}", style = MaterialTheme.typography.bodySmall, color = zColor)
                    }
                }

                // Fun toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("COZY & FUN DUO", color = palette.accentSecondary, style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle")) {
                        Text(if (state.cozyMode) "Fun 모드" else "Cozy 모드", color = palette.accentSecondary)
                    }
                }

                // Snack selection for Passenger
                if (!state.cozyMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("호두과자 vs 핫도그 밸런스 토크", style = MaterialTheme.typography.labelSmall, color = palette.accentSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onSnack("호두과자") },
                                modifier = Modifier.testTag("snackWalnut")
                            ) {
                                Text("호두과자")
                            }
                            OutlinedButton(onClick = { onSnack("핫도그") }) {
                                Text("핫도그")
                            }
                        }
                        state.snackChoice?.let {
                            Text("선택: $it (단맛 충전)", color = palette.accentSecondary, modifier = Modifier.testTag("snackChoice"))
                        }
                        OutlinedButton(
                            onClick = onEndConversation,
                            modifier = Modifier.testTag("conversationEnd")
                        ) {
                            Text("대화 마치고 음악 복원")
                        }
                    }
                }
            }
        }
    }
}
