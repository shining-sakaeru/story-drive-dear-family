package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.*

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
    val palette = LocalThemePalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top 2 Cards: Driver (Left) and Passenger (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Driver Card
            val driverAudio = state.audio.first { it.zone == SeatZone.DRIVER_1L }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_DRIVER_1L"),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.5f))
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
                        Text("1열 운전석 · PEACE SHIELD", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = onMute, modifier = Modifier.testTag("mute")) {
                                Text(if (voice.muted) "소리 켜기" else "음소거", color = palette.accentPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = palette.accentPrimary.copy(alpha = 0.15f)
                            ) {
                                Text("100% 무간섭", color = palette.accentPrimary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp, 2.dp))
                            }
                        }
                    }
                    Text("안전 전방 주시 유지 중\n모든 가족 케어는 후석과 조수석에 자동 조율됩니다.", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                    Text("음성 안내 · " + voice.message, style = MaterialTheme.typography.labelSmall, color = palette.secondaryText, modifier = Modifier.testTag("ttsStatus"))
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("audio_DRIVER_1L"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("내비게이션 오디오", style = MaterialTheme.typography.bodySmall)
                        Text("${driverAudio.volume}% · 전방 라우팅", color = palette.accentPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Passenger Card
            val passengerAudio = state.audio.first { it.zone == SeatZone.PASSENGER_1R }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_PASSENGER_1R"),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentSecondary.copy(alpha = 0.5f))
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
                        Text("1열 조수석 · COZY LOUNGE", color = palette.accentSecondary, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onToggleFun, modifier = Modifier.testTag("funToggle")) {
                            Text(if (state.cozyMode) "게임 켜기" else "대화 복원", color = palette.accentSecondary)
                        }
                    }
                    if (!state.cozyMode) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onSnack("호두과자") }, modifier = Modifier.testTag("snackWalnut")) {
                                Text("호두과자")
                            }
                            OutlinedButton(onClick = onEndConversation, modifier = Modifier.testTag("conversationEnd")) {
                                Text("대화 종료")
                            }
                        }
                        state.snackChoice?.let {
                            Text("선택: $it", color = palette.accentSecondary, modifier = Modifier.testTag("snackChoice"), style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text("부드러운 Lo-Fi 앰비언트 사운드 재생 중\n대화 시 자동으로 음악 볼륨이 덕킹(25%)됩니다.", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("audio_PASSENGER_1R"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("조수석 미디어", style = MaterialTheme.typography.bodySmall)
                        Text("${passengerAudio.volume}% · ${passengerAudio.status}", color = palette.accentSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Bottom 2 Cards: 2nd Row Kids (Left) and 3rd Row Parents (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2nd Row Kids Card
            val kidsSeat = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
            val kidsAudio = state.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_KIDS_2ND_ROW"),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentTertiary.copy(alpha = 0.5f))
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
                        Text("2열 ${state.persona.title} · ${state.persona.badge}", color = palette.accentTertiary, fontWeight = FontWeight.Bold)
                        Text("${state.noiseDb} dB", color = if (state.noiseDb > 75) palette.accentWarm else palette.accentTertiary, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        when {
                            !kidsSeat.ambientOn -> "조명 OFF · 꿀잠 수면 케어 모드 가동 중"
                            kidsSeat.status == "힐링·몰입" -> "학업 피로 완화 Lo-Fi 비트 & 21.5°C 에어케어 작동 중"
                            kidsSeat.active -> "외기 순환 · 매직 위스퍼(비밀 터널 귓속말 미션) 작동 중"
                            else -> "대기 중 · 소란 또는 피로 감지 시 자동 케어 전환"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("audio_KIDS_2ND_ROW"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("키즈 오디오북", style = MaterialTheme.typography.bodySmall)
                        Text("${kidsAudio.volume}% · ${kidsAudio.status}", color = palette.accentTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 3rd Row Parents Card
            val parentsSeat = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
            val parentsAudio = state.audio.first { it.zone == SeatZone.PARENTS_3RD_ROW }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("seat_PARENTS_3RD_ROW"),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentWarm.copy(alpha = 0.5f))
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
                        Text("3열 부모님 · 효 버틀러", color = palette.accentWarm, fontWeight = FontWeight.Bold)
                        Text("${state.elapsedMin}분 경과", color = palette.accentWarm, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        when {
                            parentsSeat.heatingLevel > 0 -> "시트 온열 ${parentsSeat.heatingLevel}단 + 요추 케어(Lumbar) 작동 중"
                            parentsSeat.active -> "장시간 탑승 피로 완화 케어 중"
                            else -> "맞춤형 온열 및 휴게소 자동 제안 대기"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                    if (state.restStopVisible) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onRestStop(true) }, modifier = Modifier.testTag("restAccept")) {
                                Text("휴게소 경유")
                            }
                            state.restStopAccepted?.let {
                                Text(if (it) "모의 경유 완료" else "통과", color = palette.accentWarm, modifier = Modifier.testTag("restAnswer"))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("audio_PARENTS_3RD_ROW"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("3열 사운드", style = MaterialTheme.typography.bodySmall)
                        Text("${parentsAudio.volume}% · ${parentsAudio.status}", color = palette.accentWarm, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
