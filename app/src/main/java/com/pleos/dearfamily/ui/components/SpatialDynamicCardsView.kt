package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.R
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.CareTab
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatStatus
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus
import kotlin.math.roundToInt

private val Bg = Color(0xFF080D16)
private val Cyan = Color(0xFF00E5FF)
private val Coral = Color(0xFFFF6B4A)
private val Violet = Color(0xFFB388FF)
private val Gold = Color(0xFFFFD54F)

@Composable
fun SpatialDynamicCardsView(
    state: FamilyCareUiState,
    voice: VoiceStatus,
    onMute: () -> Unit,
    onStoryPlayback: () -> Unit,
    onTabSelect: (CareTab) -> Unit,
    onScenarioSelect: (Scenario) -> Unit,
    onVentilationChange: (Int) -> Unit,
    onHeatingToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val driver = state.seats.first { it.zone == SeatZone.DRIVER_1L }
    val passenger = state.seats.first { it.zone == SeatZone.PASSENGER_1R }
    val kids = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
    val parents = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
    BoxWithConstraints(modifier.fillMaxSize().background(Bg).testTag("spatialContent")) {
        val narrow = maxWidth < 900.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
        val viewportHeight = maxHeight
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ScenarioHeader(state, onScenarioSelect)
            if (narrow) {
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DriverCard(driver, voice, onMute, Modifier.fillMaxWidth().height(viewportHeight * .6f))
                    PassengerCard(passenger, state, onStoryPlayback, onVentilationChange, Modifier.fillMaxWidth().height(viewportHeight * .6f))
                    KidsCard(kids, Modifier.fillMaxWidth().height(viewportHeight * .6f))
                    ParentsCard(parents, onHeatingToggle, Modifier.fillMaxWidth().height(viewportHeight * .6f))
                }
            } else {
                Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        DriverCard(driver, voice, onMute, Modifier.weight(1f).fillMaxHeight())
                        PassengerCard(passenger, state, onStoryPlayback, onVentilationChange, Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        KidsCard(kids, Modifier.weight(1f).fillMaxHeight())
                        ParentsCard(parents, onHeatingToggle, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
            Text(if (state.selectedTab == CareTab.NAVIGATION) "Navigation 선택 · 실제 경로 변경 없음 (MOCK)" else "${state.selectedTab.label} 선택 · Mock presentation", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB7C4D4))
            SpatialDock(state.selectedTab, onTabSelect, Modifier.fillMaxWidth(if (narrow) 1f else .7f).align(Alignment.CenterHorizontally).height((viewportHeight * .13f).coerceAtLeast(64.dp)))
        }
    }
}

@Composable
private fun ScenarioHeader(state: FamilyCareUiState, onSelect: (Scenario) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.heightIn(min = 48.dp).testTag("scenarioPill")) { Text("MOCK PRESENTATION · ${state.selectedScenario}", fontSize = 16.sp) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Scenario.entries.forEach { scenario ->
                DropdownMenuItem(text = { Text(scenarioLabel(scenario), fontSize = 16.sp) }, onClick = { onSelect(scenario); expanded = false }, modifier = Modifier.testTag("scenario_${scenario.name}"))
            }
        }
    }
}

@Composable
private fun SpatialCard(zone: SeatZone, modifier: Modifier, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.testTag("seat_${zone.name}"), shape = RoundedCornerShape(22.dp), color = Color(0xFF101520), border = androidx.compose.foundation.BorderStroke(1.dp, accent), shadowElevation = 8.dp) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(accent.copy(alpha = .12f), Color.Transparent))).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun DriverCard(seat: SeatStatus, voice: VoiceStatus, onMute: () -> Unit, modifier: Modifier) = SpatialCard(SeatZone.DRIVER_1L, modifier, Cyan) {
    Text("Driver Peace Shield", fontSize = 26.sp, lineHeight = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("모의 정숙 케어 · 실제 ANC 아님", fontSize = 14.sp, lineHeight = 20.sp, color = Cyan)
        Text("Good · 모의 휴식 상태", fontSize = 24.sp, lineHeight = 28.sp, color = Color(0xFFA4DFB5))
        Text(if (voice.ready) "오프라인 음성 준비됨" else "음성 준비 안 됨 · 자막 안내",
            fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFFB7C4D4))
    }
    Button(onMute, Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("quickMute")
        .semantics { selected = voice.muted }, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color.Black)) {
        Icon(painterResource(R.drawable.ic_spatial_quick_mute), "앱 음성 음소거", Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(if (voice.muted) "음소거 해제" else "QUICK MUTE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PassengerCard(seat: SeatStatus, state: FamilyCareUiState, onStory: () -> Unit, onVentilation: (Int) -> Unit, modifier: Modifier) = SpatialCard(SeatZone.PASSENGER_1R, modifier, Coral) {
    Text("Co-Pilot Lounge", fontSize = 26.sp, lineHeight = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(painter = painterResource(R.drawable.img_spatial_album), contentDescription = "원본 앨범 참고 이미지 · 동화 시연",
            modifier = Modifier.weight(.30f).fillMaxHeight(), contentScale = ContentScale.Fit)
        Column(Modifier.weight(.48f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Star Journey", fontSize = 22.sp, lineHeight = 26.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("오프라인 동화 · 모의", fontSize = 14.sp, lineHeight = 20.sp, color = Coral)
                if (state.storyPlaying) {
                    Text(state.flow.gleoTtsScript, fontSize = 16.sp, lineHeight = 22.sp,
                        color = Color.White, modifier = Modifier.testTag("spatialStoryCaption"))
                }
            }
            Button(onStory, Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("spatialPlay")
                .semantics { selected = state.storyPlaying }, enabled = !state.quietCabin && state.silentMessage == null,
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Color.Black)) {
                Text(if (state.storyPlaying) "Stop Story" else "Play Story", fontSize = 18.sp)
            }
        }
        VerticalVentilation(seat.fanLevel, onVentilation, Modifier.weight(.22f).fillMaxHeight())
    }
}

@Composable
private fun VerticalVentilation(value: Int, onVentilation: (Int) -> Unit, modifier: Modifier) {
    val safe = value.coerceIn(0, 3)
    val update: (Float) -> Unit = { raw -> if (raw.isFinite()) onVentilation(raw.roundToInt().coerceIn(0, 3)) }
    Column(modifier.testTag("seatVentilation").semantics {
        contentDescription = "좌석 통풍 · MOCK"
        progressBarRangeInfo = ProgressBarRangeInfo(safe.toFloat(), 0f..3f, 2)
        setProgress { raw -> if (!raw.isFinite()) false else { update(raw); true } }
    }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(R.drawable.ic_spatial_seat_ventilation), null, Modifier.size(24.dp), tint = Coral)
        Text("$safe / 3", fontSize = 20.sp, lineHeight = 24.sp, color = Coral)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
            detectTapGestures { offset -> if (size.height > 0) update((1f - offset.y / size.height) * 3f) }
        }.pointerInput(Unit) {
            detectVerticalDragGestures { change, _ ->
                if (size.height > 0) update((1f - change.position.y / size.height) * 3f)
                change.consume()
            }
        }) {
            val thumbY = (maxHeight - 20.dp).coerceAtLeast(0.dp) * (1f - safe / 3f)
            Box(Modifier.align(Alignment.Center).width(16.dp).fillMaxHeight().background(Color(0xFF584A49), RoundedCornerShape(8.dp)))
            Box(Modifier.align(Alignment.BottomCenter).width(16.dp).fillMaxHeight(safe / 3f).background(Coral, RoundedCornerShape(8.dp)))
            Box(Modifier.align(Alignment.TopCenter).offset(y = thumbY).size(24.dp, 20.dp).background(Color(0xFFFFDACD), RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
private fun KidsCard(seat: SeatStatus, modifier: Modifier) = SpatialCard(SeatZone.KIDS_2ND_ROW, modifier, Violet) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("2nd Row Kids Dreamland", Modifier.weight(1f), fontSize = 26.sp, lineHeight = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Image(painterResource(R.drawable.img_spatial_story), "Magic Galaxy 참고 artwork", Modifier.size(40.dp))
    }
    Image(painter = painterResource(R.drawable.img_spatial_waveform), contentDescription = "원본 모의 파형 · 실제 오디오 측정 아님",
        modifier = Modifier.fillMaxWidth().weight(1f), contentScale = ContentScale.Fit)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(if (!seat.ambientOn) "Magic Galaxy · 조명 OFF" else if (seat.freshAir) "Magic Galaxy · 외기 순환" else "Magic Galaxy · 동화 대기",
            fontSize = 16.sp, lineHeight = 22.sp, color = Violet)
        Text("${seat.noiseDb} dB · ${seat.status} · MOCK", fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFFD2C4DE))
    }
}

@Composable
private fun ParentsCard(seat: SeatStatus, onHeating: () -> Unit, modifier: Modifier) = SpatialCard(SeatZone.PARENTS_3RD_ROW, modifier, Gold) {
    Text("3rd Row Comfort", fontSize = 26.sp, lineHeight = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconToggleButton(checked = seat.heatingLevel > 0, onCheckedChange = { onHeating() },
            modifier = Modifier.size(48.dp).testTag("seatHeating")) {
            Icon(painterResource(R.drawable.ic_spatial_seat_heating), "모의 열선 ${if (seat.heatingLevel > 0) "끄기" else "켜기"}", tint = Gold)
        }
        Text("온열 ${seat.heatingLevel} · 팬 ${seat.fanLevel}", fontSize = 20.sp, lineHeight = 24.sp, color = Gold)
        Icon(painterResource(R.drawable.ic_spatial_hvac), "공기 정화 참고 아이콘", tint = Gold)
    }
    Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Text("MOCK\n8 AQI", Modifier.weight(1f), fontSize = 22.sp, lineHeight = 26.sp, color = Color(0xFFB4DFA8))
        Image(painter = painterResource(R.drawable.img_spatial_comfort_curve), contentDescription = "원본 쾌적도 샘플 그래프 · 센서 이력 아님",
            modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Fit)
    }
}

@Composable
private fun SpatialDock(selected: CareTab, onSelect: (CareTab) -> Unit, modifier: Modifier) = Surface(modifier, shape = RoundedCornerShape(32.dp), color = Color(0xDD182332)) {
    Row(Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        CareTab.entries.forEach { tab ->
            val active = selected == tab
            TextButton(onClick = { onSelect(tab) }, modifier = Modifier.weight(1f).fillMaxHeight().testTag("dock_${tab.name}").semantics { this.selected = active }, colors = ButtonDefaults.textButtonColors(containerColor = if (active) Cyan.copy(alpha = .2f) else Color.Transparent, contentColor = if (active) Cyan else Color.White)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(painterResource(tabIcon(tab)), tab.label, modifier = Modifier.size(24.dp)); Text(tab.label, fontSize = 14.sp) }
            }
        }
    }
}

private fun tabIcon(tab: CareTab) = when (tab) { CareTab.HOME -> R.drawable.ic_spatial_home; CareTab.CLIMATE -> R.drawable.ic_spatial_hvac; CareTab.MEDIA -> R.drawable.ic_spatial_media; CareTab.NAVIGATION -> R.drawable.ic_spatial_navigation }
private fun scenarioLabel(scenario: Scenario) = when (scenario) { Scenario.KIDS_NOISE -> "Kids Noise"; Scenario.KIDS_SLEEP -> "Kids Sleep"; Scenario.TEEN_FOCUS -> "Teen Focus"; Scenario.PARENTS -> "Parents Care"; Scenario.PASSENGER -> "Passenger Cozy" }
