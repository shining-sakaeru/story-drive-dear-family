package com.pleos.dearfamily.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.platform.AudioNarrator
import com.pleos.dearfamily.platform.VehicleReadState
import com.pleos.dearfamily.ui.components.*
import com.pleos.dearfamily.ui.theme.*

@Composable fun DearFamilyMainScreen(narrator: AudioNarrator, vm: DearFamilyViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val safety by vm.safety.collectAsStateWithLifecycle()
    val vehicle by vm.vehicle.collectAsStateWithLifecycle()
    var lastHandledEvent by rememberSaveable { mutableLongStateOf(-1L) }
    LaunchedEffect(state.eventId, safety.canPlayAudio) {
        narrator.setAllowed(safety.canPlayAudio)
        if (!safety.canPlayAudio) {
            lastHandledEvent = state.eventId
            narrator.stop()
        } else if (lastHandledEvent != state.eventId) {
            lastHandledEvent = state.eventId
            if (state.silentMessage != null) narrator.silenceForSleep()
            else narrator.speak(state.eventId, state.flow.gleoTtsScript)
        }
    }
    if (safety.canInteract) {
        DemoDashboard(state, vehicle, narrator, vm)
    } else {
        Column(Modifier.fillMaxSize().background(Background).padding(24.dp)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("DEAR FAMILY", style = MaterialTheme.typography.headlineSmall, color = Emerald)
            Text("가족은 편안하게, 운전은 평온하게", style = MaterialTheme.typography.titleMedium)
            Text(safety.sourceMessage, color = SecondaryText, modifier = Modifier.testTag("restrictedStatus"))
            Text("주행 제한 또는 상태 미확인: 게임·상황 조작·시연 애니메이션·음성을 중지합니다.", color = SecondaryText)
            state.seats.forEach { seat ->
                Text("${seat.zone.label} · ${seat.status} (MOCK)", color = androidx.compose.ui.graphics.Color(seat.zone.accent),
                    modifier = Modifier.testTag("seat_${seat.zone.name}"))
            }
            Text("모든 가족 케어 제어는 시뮬레이션입니다.", color = SecondaryText,
                modifier = Modifier.testTag("simulationNotice"))
            if (safety.demoBuild && !safety.automotive)
                Button(vm::enterDemo, Modifier.testTag("enterDemo")) { Text("정차 모의 시연 시작") }
            VehicleReadout(vehicle, null)
        }
    }
}

@Composable private fun DemoDashboard(state: FamilyCareUiState, vehicle: VehicleReadState,
    narrator: AudioNarrator, vm: DearFamilyViewModel) {
    val voice by narrator.status.collectAsStateWithLifecycle()
    val breathing by rememberInfiniteTransition(label = "peace shield").animateFloat(.16f, .46f,
        infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "calm breathing")
    val sleepFlash = remember { Animatable(0f) }
    LaunchedEffect(state.sleepPulseId) {
        sleepFlash.snapTo(0f)
        if (state.sleepPulseId > 0) {
            repeat(2) { sleepFlash.animateTo(.9f, tween(450)); sleepFlash.animateTo(0f, tween(450)) }
        }
    }
    val rearActive = state.seats.any { it.active && (it.zone == SeatZone.KIDS_2ND_ROW || it.zone == SeatZone.PARENTS_3RD_ROW) }
    val sleeping = state.seats.any { it.zone == SeatZone.KIDS_2ND_ROW && !it.ambientOn }
    val edge = if (sleepFlash.value > 0f) sleepFlash.value else if (sleeping) .22f else if (rearActive) breathing else .1f
    Box(Modifier.fillMaxSize().background(Background).drawWithContent {
        drawContent()
        drawRoundRect(Emerald.copy(alpha = edge * .13f), Offset(4.dp.toPx(), 4.dp.toPx()),
            Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()), CornerRadius(22.dp.toPx()), style = Stroke(15.dp.toPx()))
        drawRoundRect(Emerald.copy(alpha = edge), Offset(3.dp.toPx(), 3.dp.toPx()),
            Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()), CornerRadius(18.dp.toPx()), style = Stroke(4.dp.toPx()))
    }) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 1100.dp && maxHeight >= 650.dp
        val compactDock = maxHeight < 560.dp
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (wide) TopPeaceShieldBar() else {
                Text("DEAR FAMILY · 정차 시연", color = Emerald, style = MaterialTheme.typography.titleMedium)
                Text("가족 케어는 MOCK · 실제 차량 제어 없음", color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("simulationNotice"))
            }
            if (state.tourReport) Text("ZERO-STRESS REPORT  ·  가족 3개 존 케어 완료 / 운전석 내비 라우팅 유지 (모의)",
                color = Emerald, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("tourReport"))
            state.silentMessage?.let { Text(it, color = Emerald, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("silentSleepMessage")) }
            if (wide) {
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CabinDigitalTwinView(state, Modifier.weight(.35f).fillMaxHeight())
                    IpoPipelineMonitor(state.flow, state.eventId, Modifier.weight(.38f).fillMaxHeight())
                    MultiZoneControllerPanel(state, voice, narrator::toggleMute, vm::toggleMode,
                        vm::chooseSnack, vm::endConversation, vm::answerRestStop, Modifier.weight(.27f).fillMaxHeight())
                }
            } else {
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("가족", "I-P-O", "미디어").forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                            text = { Text(title) }, modifier = Modifier.testTag("panelTab$index"))
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> CabinDigitalTwinView(state, Modifier.fillMaxSize())
                        1 -> IpoPipelineMonitor(state.flow, state.eventId, Modifier.fillMaxSize())
                        else -> MultiZoneControllerPanel(state, voice, narrator::toggleMute, vm::toggleMode,
                            vm::chooseSnack, vm::endConversation, vm::answerRestStop, Modifier.fillMaxSize())
                    }
                }
            }
            if (vehicle.status != com.pleos.dearfamily.platform.VehicleLinkStatus.MOCK_ONLY)
                VehicleReadout(vehicle, vm::refreshVehicle)
            ScenarioInjectorDock(state, vm::manual, vm::startTour, vm::stopTour, vm::reset,
                vm::setNoise, vm::setElapsed, compact = compactDock)
        }
        }
    }
}

@Composable private fun VehicleReadout(state: VehicleReadState, refresh: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth().heightIn(max = 125.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("VEHICLE READ-ONLY · ${state.message}", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        state.occupancy.forEach { (area, occupied) ->
            Text("$area: ${when (occupied) { true -> "탑승"; false -> "비어 있음"; null -> "알 수 없음" }}",
                style = MaterialTheme.typography.bodySmall, color = Emerald)
        }
        refresh?.let { OutlinedButton(it, Modifier.testTag("refreshVehicle")) { Text("실제 SDK 다시 조회") } }
    }
}
