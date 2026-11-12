package com.pleos.dearfamily.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.model.*
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.abs
import kotlin.math.sin

@Composable fun MultiZoneControllerPanel(state: FamilyCareUiState, voice: VoiceStatus,
    onMute: () -> Unit, onToggleFun: () -> Unit, onSnack: (String) -> Unit,
    onEndConversation: () -> Unit, onRestStop: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val phase by rememberInfiniteTransition(label = "gleo").animateFloat(0f, 6.28f,
        infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "voice bars")
    CarePanel(modifier) {
        PanelHeading("03", "서로 다른 취향, 같은 여행", "PRIVATE ZONE · SHARED MEMORIES", Amber)
        // The debug overlay owns vertical scrolling. A second viewport here left
        // controls clipped even after performScrollTo(), so physical clicks missed.
        Column(Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Gleo 브리핑", style = MaterialTheme.typography.titleMedium)
                    Text("시나리오 스크립트 · AI 호출 없음", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onMute, modifier = Modifier.heightIn(min = 48.dp).testTag("mute")) {
                    Text(if (voice.muted) "음성 켜기" else "음소거")
                }
            }
            val amberColor = Amber
            val borderCol = CardBorder
            Canvas(Modifier.fillMaxWidth().height(32.dp)) {
                repeat(30) { i ->
                    val x = size.width * (i + .5f) / 30f
                    val height = if (voice.speaking) (abs(sin(i * .8f + phase)) * .8f + .1f) * size.height else 2.dp.toPx()
                    drawLine(if (voice.speaking) amberColor else borderCol, Offset(x, (size.height - height) / 2),
                        Offset(x, (size.height + height) / 2), 3.dp.toPx())
                }
            }
            Text(voice.message, color = if (voice.ready) Amber else SecondaryText,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("ttsStatus"))
            Text(state.flow.gleoTtsScript.ifBlank { "소리 대신 따뜻한 배려가 흐르는 중입니다." },
                style = MaterialTheme.typography.bodyMedium)
            Text("음성은 기기 공용 스피커 시연입니다. 실제 독립 음장 출력이 아닙니다.",
                color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder)
            Text("ZONE AUDIO ROUTING", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            state.audio.forEach { zone ->
                val color = Color(zone.zone.accent)
                Column(Modifier.fillMaxWidth().testTag("audio_${zone.zone.name}").semantics(mergeDescendants = true) {},
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(zone.zone.label, color = color, style = MaterialTheme.typography.bodyMedium)
                        Text("${zone.volume}%", color = color, style = MaterialTheme.typography.bodySmall)
                    }
                    Box(Modifier.fillMaxWidth().height(3.dp).background(CardBorder, RoundedCornerShape(2.dp))) {
                        Box(Modifier.fillMaxWidth((zone.volume / 100f).coerceIn(0f, 1f)).fillMaxHeight()
                            .background(color, RoundedCornerShape(2.dp)))
                    }
                    Text(zone.status, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider(color = CardBorder)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("COZY & FUN DUO", color = Amber, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = onToggleFun, modifier = Modifier.heightIn(min = 48.dp).testTag("funToggle")) {
                    Text(if (state.cozyMode) "Fun 모드" else "Cozy 모드")
                }
            }
            if (!state.cozyMode) {
                Column(Modifier.background(Amber.copy(alpha = .08f), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("우리 부부 이구동성 밸런스 게임", color = Amber, style = MaterialTheme.typography.titleMedium)
                    Text("Q. 휴게소 최애 간식은?", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton({ onSnack("호두과자") }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("snackWalnut")) { Text("호두과자") }
                    OutlinedButton({ onSnack("알감자") }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("snackPotato")) { Text("알감자") }
                    state.snackChoice?.let { Text("우리의 선택: $it", color = Amber, modifier = Modifier.testTag("snackChoice")) }
                }
            } else Text("따뜻한 앰버 조명, 빗소리와 로파이. 대화 중에는 25%로 낮춥니다.",
                style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            OutlinedButton(onClick = onEndConversation, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("conversationEnd")) {
                Text("대화 종료 · 음악 70% 복원", style = MaterialTheme.typography.bodySmall)
            }
            if (state.restStopVisible) {
                Column(Modifier.background(Coral.copy(alpha = .08f), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("부모님 휴식을 위해 10분 뒤 평창휴게소를 경유할까요?",
                        color = Coral, style = MaterialTheme.typography.bodyMedium)
                    Text("가상 위치·예상 시간 · 실제 내비 변경 없음", color = SecondaryText,
                        style = MaterialTheme.typography.bodySmall)
                    if (state.restStopAccepted == null) {
                        OutlinedButton({ onRestStop(true) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("restAccept")) { Text("모의 경유 추가") }
                        TextButton({ onRestStop(false) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("지금은 괜찮아요") }
                    } else Text(if (state.restStopAccepted == true) "모의 경유가 추가되었습니다." else "기존 모의 경로를 유지합니다.",
                        modifier = Modifier.testTag("restAnswer"), color = Coral)
                }
            }
        }
    }
}
