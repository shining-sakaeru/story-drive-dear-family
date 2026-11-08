package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.roundToInt

@Composable fun ScenarioInjectorDock(state: FamilyCareUiState, onScenario: (Scenario) -> Unit,
    onTour: () -> Unit, onStop: () -> Unit, onReset: () -> Unit,
    onNoise: (Int) -> Unit, onElapsed: (Int) -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    CarePanel(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onTour, Modifier.heightIn(min = 48.dp).testTag("tour"),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Background)) {
                Text(if (state.tourRunning) "데모 재시작 · ${state.tourElapsedSeconds}s" else "전체 가족 케어 · 30초")
            }
            DockButton("2열 아이 소란 · 82dB", "kidsNoise", { onScenario(Scenario.KIDS_NOISE) })
            DockButton("2열 아이 수면", "kidsSleep", { onScenario(Scenario.KIDS_SLEEP) })
            DockButton("3열 부모님 · 52분", "parents", { onScenario(Scenario.PARENTS) })
            DockButton("조수석 Cozy / Fun", "passenger", { onScenario(Scenario.PASSENGER) })
            if (state.tourRunning) DockButton("데모 중지", "tourStop", onStop)
            TextButton(onReset, Modifier.heightIn(min = 48.dp).testTag("reset")) { Text("초기화") }
        }
        if (!compact) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            SensorSlider("2열 소음", "${state.noiseDb} dB", "75dB 초과 · 터널 300m 모의 조건",
                state.noiseDb.toFloat(), 30f..100f, "noiseSlider", { onNoise(it.roundToInt()) }, Modifier.weight(1f))
            SensorSlider("주행 경과", "${state.elapsedMin}분", "50분 초과 · 체압 변화 모의 조건",
                state.elapsedMin.toFloat(), 0f..120f, "elapsedSlider", { onElapsed(it.roundToInt()) }, Modifier.weight(1f))
        }
    }
}

@Composable private fun DockButton(label: String, tag: String, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.heightIn(min = 48.dp).testTag(tag)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun SensorSlider(label: String, valueLabel: String, detail: String, value: Float,
    range: ClosedFloatingPointRange<Float>, tag: String, onChange: (Float) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            Text(valueLabel, color = Emerald, style = MaterialTheme.typography.bodySmall)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range,
            modifier = Modifier.fillMaxWidth().height(32.dp).testTag(tag))
        Text(detail, color = SecondaryText, style = MaterialTheme.typography.labelSmall)
    }
}
