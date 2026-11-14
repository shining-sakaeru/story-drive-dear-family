package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.platform.VoiceStatus
import com.pleos.dearfamily.ui.theme.CarePanel
import com.pleos.dearfamily.ui.theme.LocalThemePalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FamilyJourneyShowcasePanel(
    state: FamilyCareUiState,
    voice: VoiceStatus,
    onScenario: (Scenario) -> Unit,
    onStop: () -> Unit,
    onRestStop: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var beforeState by remember { mutableStateOf<FamilyCareUiState?>(null) }
    var detailsOpen by remember { mutableStateOf(false) }
    var flashScenario by remember { mutableStateOf<Scenario?>(null) }
    val palette = LocalThemePalette.current
    val before = beforeState
    LaunchedEffect(flashScenario) {
        if (flashScenario != null) {
            delay(650)
            flashScenario = null
        }
    }

    CarePanel(modifier.testTag("journeyPanel"), highlight = false) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("가족 여정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("정차 상태에서만 상세 케어를 시연합니다", color = palette.secondaryText, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyClose")) { Text("닫기") }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JourneyButton("소란", "journeyNoise", Scenario.KIDS_NOISE, flashScenario) { flashScenario = it; beforeState = state; onScenario(it) }
            JourneyButton("수면", "journeySleep", Scenario.KIDS_SLEEP, flashScenario) { flashScenario = it; beforeState = state; onScenario(it) }
            JourneyButton("부모님", "journeyParents", Scenario.PARENTS, flashScenario) { flashScenario = it; beforeState = state; onScenario(it) }
            OutlinedButton(onClick = onStop, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyStop")) { Text("중지") }
            OutlinedButton(onClick = { detailsOpen = true }, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyDetails")) { Text("자세히") }
        }
        before?.let { previous ->
            HorizontalDivider(color = palette.cardBorder)
            val beforeSeat = previous.seats.firstOrNull { it.zone == state.flow.targetZone }
            val afterSeat = state.seats.firstOrNull { it.zone == state.flow.targetZone }
            val beforeAudio = previous.audio.firstOrNull { it.zone == state.flow.targetZone }
            val afterAudio = state.audio.firstOrNull { it.zone == state.flow.targetZone }
            Text("전: ${state.flow.targetZone.label} · ${beforeSeat?.status}", color = palette.secondaryText,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("journeyBeforeAfter"))
            Text("후: ${state.flow.activeScenarioTitle} · ${state.flow.targetZone.label}", color = palette.primaryText,
                style = MaterialTheme.typography.bodySmall)
            Text("좌석 상태 · 온도 ${beforeSeat?.temperature}°C → ${afterSeat?.temperature}°C · 풍량 ${beforeSeat?.fanLevel} → ${afterSeat?.fanLevel} · 조명 ${beforeSeat?.ambientOn} → ${afterSeat?.ambientOn} · 열선 ${beforeSeat?.heatingLevel} → ${afterSeat?.heatingLevel}",
                color = palette.secondaryText, style = MaterialTheme.typography.bodySmall)
            Text("오디오 상태 · 모의 음량 ${beforeAudio?.volume}% → ${afterAudio?.volume}% · ${afterAudio?.status}",
                color = palette.secondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Text("입력: 센서 상태 LOCAL MOCK · 제어: 차량 출력 LOCAL MOCK · 음성 출처: 실제 앱 AudioNarrator", color = palette.secondaryText,
            style = MaterialTheme.typography.bodySmall)
        Text(
            when {
                state.selectedScenario == Scenario.KIDS_SLEEP.name -> "수면 케어: 무음 상태 · TTS 없음"
                voice.speaking -> "실제 앱 음성 재생 중 · ${voice.message}"
                !voice.ready -> "음성 오류/준비 전 · 자막으로 안내: ${voice.message}"
                else -> voice.message
            },
            color = if (voice.speaking) palette.accentPrimary else palette.secondaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("journeyVoice")
        )
        if (state.flow.gleoTtsScript.isNotBlank()) {
            Text(state.flow.gleoTtsScript, style = MaterialTheme.typography.bodyMedium,
                color = palette.primaryText, modifier = Modifier.testTag("journeyCaption"))
        }
        if (state.restStopVisible) {
            Surface(color = palette.accentWarm.copy(alpha = .1f), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("부모님 휴게소 경유를 제안합니다", style = MaterialTheme.typography.bodyMedium)
                    if (state.restStopAccepted == null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onRestStop(true) }, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyRestAccept")) { Text("수락") }
                            TextButton(onClick = { onRestStop(false) }, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyRestReject")) { Text("거절") }
                        }
                    } else Text(if (state.restStopAccepted == true) "경유 제안을 수락했습니다." else "경유 제안을 거절했습니다.")
                }
            }
        }
    }

    if (detailsOpen) {
        JourneyDetailsDialog(state, voice) { detailsOpen = false }
    }
}

@Composable
private fun JourneyButton(
    label: String,
    tag: String,
    scenario: Scenario,
    flashScenario: Scenario?,
    onClick: (Scenario) -> Unit
) {
    val selected = flashScenario == scenario
    OutlinedButton(
        onClick = { onClick(scenario) },
        border = BorderStroke(1.dp, if (selected) LocalThemePalette.current.accentPrimary else LocalThemePalette.current.cardBorder),
        modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag(tag)
    ) { Text(if (selected) "✓ $label" else label) }
}

@Composable
private fun JourneyDetailsDialog(state: FamilyCareUiState, voice: VoiceStatus, onClose: () -> Unit) {
    val palette = LocalThemePalette.current
    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.testTag("journeyDetailsDialog"),
        confirmButton = { TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).testTag("journeyDetailsClose")) { Text("닫기") } },
        title = { Text("선택한 케어 상세") },
        text = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val viewport = maxHeight * 0.72f
                Column(
                    Modifier.heightIn(max = viewport).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                Text(state.flow.activeScenarioTitle, style = MaterialTheme.typography.titleMedium)
                Text("입력 센서", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                state.flow.inputSensors.forEach { sensor ->
                    Text("${sensor.sdkName} · ${sensor.sensorKey}: ${sensor.currentValue} · 충족 ${if (sensor.isTriggered) "예" else "아니오"}", style = MaterialTheme.typography.bodySmall)
                }
                Text("처리 규칙", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                Text(state.flow.processingRule, style = MaterialTheme.typography.bodySmall)
                Text("Mock 출력", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                state.flow.vehicleControlOutputs.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(state.flow.audioRoutingOutput, style = MaterialTheme.typography.bodySmall)
                Text("전후 상태", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                Text("현재 좌석: ${state.flow.targetZone.label} · 현재 시나리오: ${state.selectedScenario}", style = MaterialTheme.typography.bodySmall)
                Text("음성: ${voice.message}", style = MaterialTheme.typography.bodySmall)
                if (state.flow.gleoTtsScript.isNotBlank()) {
                    Text("안내 본문", color = palette.accentPrimary, fontWeight = FontWeight.Bold)
                    Text(state.flow.gleoTtsScript, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        }
    )
}
