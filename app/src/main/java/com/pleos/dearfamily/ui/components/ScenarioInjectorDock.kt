package com.pleos.dearfamily.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.ui.theme.*
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ScenarioInjectorDock(
    state: FamilyCareUiState,
    onScenario: (Scenario) -> Unit,
    onTour: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onNoise: (Int) -> Unit,
    onElapsed: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    initiallyExpanded: Boolean = false
) {
    val palette = LocalThemePalette.current
    var isDrawerOpen by remember { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = palette.cardBackgroundElevated.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drawer Header Toggle Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.accentPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "⚡ DEMO CONSOLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accentPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        "심사 및 테스트용 시나리오 인젝터",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
                TextButton(
                    onClick = { isDrawerOpen = !isDrawerOpen },
                    modifier = Modifier.testTag("demoDrawerToggle")
                ) {
                    Text(
                        if (isDrawerOpen) "서랍 접기 ▲" else "콘솔 펼치기 ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accentPrimary
                    )
                }
            }

            AnimatedVisibility(
                visible = isDrawerOpen,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Scenario Preset Buttons
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTour,
                            enabled = !state.quietCabin,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("tour"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accentPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                if (state.tourRunning) "투어 재시작 · ${state.tourElapsedSeconds}s" else "음성 연동 투어 시작",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DockButton("2열 아이 소란 · 82dB", "kidsNoise", { onScenario(Scenario.KIDS_NOISE) })
                        DockButton("2열 아이 수면", "kidsSleep", { onScenario(Scenario.KIDS_SLEEP) })
                        DockButton("2열 청소년 힐링", "teenFocus", { onScenario(Scenario.TEEN_FOCUS) })
                        DockButton("3열 부모님 · 52분", "parents", { onScenario(Scenario.PARENTS) })
                        DockButton("조수석 Cozy / Fun", "passenger", { onScenario(Scenario.PASSENGER) })
                        if (state.tourRunning) DockButton("데모 중지", "tourStop", onStop)
                        TextButton(
                            onClick = onReset,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("reset")
                        ) {
                            Text("초기화", color = palette.secondaryText)
                        }
                    }

                    // Sliders
                    if (!compact) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            SensorSlider(
                                "2열 소음 센서",
                                "${state.noiseDb} dB",
                                "75dB 초과 시 자동 귓속말 미션 전환",
                                state.noiseDb.toFloat(),
                                30f..100f,
                                "noiseSlider",
                                { onNoise(it.roundToInt()) },
                                Modifier.weight(1f)
                            )
                            SensorSlider(
                                "주행 경과 시간",
                                "${state.elapsedMin}분",
                                "50분 초과 시 부모님 맞춤 케어 제안",
                                state.elapsedMin.toFloat(),
                                0f..120f,
                                "elapsedSlider",
                                { onElapsed(it.roundToInt()) },
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockButton(label: String, tag: String, onClick: () -> Unit) {
    val palette = LocalThemePalette.current
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag(tag),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = palette.primaryText)
    }
}

@Composable
private fun SensorSlider(
    label: String,
    valueLabel: String,
    detail: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    tag: String,
    onChange: (Float) -> Unit,
    modifier: Modifier
) {
    val palette = LocalThemePalette.current
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = palette.secondaryText, style = MaterialTheme.typography.bodySmall)
            Text(valueLabel, color = palette.accentPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag(tag),
            colors = SliderDefaults.colors(
                thumbColor = palette.accentPrimary,
                activeTrackColor = palette.accentPrimary,
                inactiveTrackColor = palette.cardBorder
            )
        )
        Text(detail, color = palette.secondaryText, style = MaterialTheme.typography.labelSmall)
    }
}
