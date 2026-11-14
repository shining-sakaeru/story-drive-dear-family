package com.pleos.dearfamily.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pleos.dearfamily.model.DriveModeState
import com.pleos.dearfamily.model.FamilyPersona
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.platform.AudioNarrator
import com.pleos.dearfamily.platform.VehicleReadState
import com.pleos.dearfamily.ui.components.*
import com.pleos.dearfamily.ui.theme.*

@Composable
fun DearFamilyMainScreen(
    narrator: AudioNarrator,
    vm: DearFamilyViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val safety by vm.safety.collectAsStateWithLifecycle()
    val vehicle by vm.vehicle.collectAsStateWithLifecycle()

    val isDriving = state.driveMode == DriveModeState.DRIVING
    val defaultModeTheme = runCatching {
        if (isDriving) ThemeMode.valueOf(state.themeConfig.drivingThemeName)
        else ThemeMode.valueOf(state.themeConfig.parkedThemeName)
    }.getOrDefault(if (isDriving) ThemeMode.GENESIS_LUXURY else ThemeMode.SCANDINAVIAN_WARM)

    var manualThemeOverride by rememberSaveable { mutableStateOf<String?>(null) }
    val currentTheme = manualThemeOverride?.let { name ->
        runCatching { ThemeMode.valueOf(name) }.getOrNull()
    } ?: defaultModeTheme

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    var lastHandledEvent by rememberSaveable { mutableLongStateOf(-1L) }
    LaunchedEffect(state.eventId, safety.canPlayAudio, state.quietCabin) {
        narrator.setAllowed(safety.canPlayAudio && !state.quietCabin)
        if (!safety.canPlayAudio || state.quietCabin) {
            lastHandledEvent = state.eventId
            narrator.stop()
        } else if (lastHandledEvent != state.eventId) {
            lastHandledEvent = state.eventId
            if (state.silentMessage != null) narrator.silenceForSleep()
            else narrator.speak(state.eventId, state.flow.gleoTtsScript)
        }
    }

    DearFamilyTheme(themeMode = currentTheme) {
        if (safety.canInteract) {
            DemoDashboard(
                state = state,
                vehicle = vehicle,
                narrator = narrator,
                vm = vm,
                themeMode = currentTheme,
                onThemeChange = { manualThemeOverride = it.name },
                isDriving = isDriving,
                onToggleDriveMode = {
                    manualThemeOverride = null
                    vm.toggleDriveMode()
                },
                showSettings = showSettingsDialog,
                onOpenSettings = { showSettingsDialog = true },
                onCloseSettings = { showSettingsDialog = false }
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("DEAR FAMILY", style = MaterialTheme.typography.headlineSmall, color = Emerald)
                Text("가족은 편안하게, 운전은 평온하게", style = MaterialTheme.typography.titleMedium)
                Text(safety.sourceMessage, color = SecondaryText, modifier = Modifier.testTag("restrictedStatus"))
                Text("주행 제한 또는 상태 미확인: 게임·상황 조작·시연 애니메이션·음성을 중지합니다.", color = SecondaryText)
                state.seats.forEach { seat ->
                    Text(
                        "${seat.zone.label} · ${seat.status} (MOCK)",
                        color = Color(seat.zone.accent),
                        modifier = Modifier.testTag("seat_${seat.zone.name}")
                    )
                }
                Text(
                    "가족 케어는 MOCK · 차량 조회는 READ-ONLY입니다.",
                    color = SecondaryText,
                    modifier = Modifier.testTag("simulationNotice")
                )
                if (safety.demoBuild && !safety.automotive) {
                    Button(vm::enterDemo, Modifier.testTag("enterDemo")) {
                        Text("정차 모의 시연 시작")
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoDashboard(
    state: FamilyCareUiState,
    vehicle: VehicleReadState,
    narrator: AudioNarrator,
    vm: DearFamilyViewModel,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    isDriving: Boolean,
    onToggleDriveMode: () -> Unit,
    showSettings: Boolean,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit
) {
    val voice by narrator.status.collectAsStateWithLifecycle()
    val palette = LocalThemePalette.current

    val breathing by rememberInfiniteTransition(label = "peace shield").animateFloat(
        .16f, .46f,
        infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "calm breathing"
    )
    val sleepFlash = remember { Animatable(0f) }
    LaunchedEffect(state.sleepPulseId) {
        sleepFlash.snapTo(0f)
        if (state.sleepPulseId > 0) {
            repeat(2) {
                sleepFlash.animateTo(.9f, tween(450))
                sleepFlash.animateTo(0f, tween(450))
            }
        }
    }
    val rearActive = state.seats.any { it.active && (it.zone == SeatZone.KIDS_2ND_ROW || it.zone == SeatZone.PARENTS_3RD_ROW) }
    val sleeping = state.seats.any { it.zone == SeatZone.KIDS_2ND_ROW && !it.ambientOn }
    val edge = if (sleepFlash.value > 0f) sleepFlash.value else if (sleeping) .22f else if (rearActive) breathing else .1f

    var showDebug by rememberSaveable { mutableStateOf(false) }
    var showJourney by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(palette.background)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    palette.accentPrimary.copy(alpha = edge * .13f),
                    Offset(4.dp.toPx(), 4.dp.toPx()),
                    Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                    CornerRadius(22.dp.toPx()),
                    style = Stroke(15.dp.toPx())
                )
                drawRoundRect(
                    palette.accentPrimary.copy(alpha = edge),
                    Offset(3.dp.toPx(), 3.dp.toPx()),
                    Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    CornerRadius(18.dp.toPx()),
                    style = Stroke(4.dp.toPx())
                )
            }
    ) {
        val journeyHeight = maxHeight * .38f
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardHeader(
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    isDriving = isDriving,
                    onToggleDriveMode = onToggleDriveMode,
                    onOpenSettings = onOpenSettings,
                    onOpenJourney = {
                        onThemeChange(ThemeMode.SCANDINAVIAN_WARM)
                        showJourney = true
                    },
                    onOpenDebug = { showDebug = true }
                )

                if (showJourney) {
                    FamilyJourneyShowcasePanel(
                        state = state,
                        voice = voice,
                        onScenario = vm::manual,
                        onStop = vm::stopTour,
                        onRestStop = vm::answerRestStop,
                        onClose = { vm.stopTour(); showJourney = false },
                        modifier = Modifier.fillMaxWidth().heightIn(max = journeyHeight)
                            .verticalScroll(rememberScrollState())
                    )
                }

                if (state.tourReport) {
                    Text(
                        "ZERO-STRESS REPORT · 가족 3개 존 케어 완료 / 운전석 내비 라우팅 유지 (모의)",
                        color = palette.accentPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("tourReport")
                    )
                }

                state.silentMessage?.let {
                    Text(
                        it,
                        color = palette.accentPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("silentSleepMessage")
                    )
                }

                // Main Content Switching based on chosen theme
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        themeMode == ThemeMode.SCANDINAVIAN_WARM -> {
                            WarmFamilyStoryView(
                                state = state,
                                voice = voice,
                                onStoryPlayback = vm::toggleStoryPlayback,
                                onCabinWarmth = vm::toggleCabinWarmth,
                                onQuietCabin = vm::toggleQuietCabin,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        themeMode == ThemeMode.SPATIAL_WIDGET -> {
                            SpatialDynamicCardsView(
                                state = state,
                                voice = voice,
                                onMute = narrator::toggleMute,
                                onStoryPlayback = vm::toggleStoryPlayback,
                                onTabSelect = vm::selectTab,
                                onScenarioSelect = vm::selectScenario,
                                onVentilationChange = vm::setSeatVentilation,
                                onHeatingToggle = vm::toggleSeatHeating,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            // GENESIS_LUXURY (Default flagship ccNC Cockpit)
                            GenesisLuxuryCockpitView(
                                state = state,
                                voice = voice,
                                onMute = narrator::toggleMute,
                                onStoryPlayback = vm::toggleStoryPlayback,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

            }
        }

        if (showDebug) {
            DebugOverlay(
                state = state,
                vehicle = vehicle,
                narrator = narrator,
                vm = vm,
                onDismiss = { showDebug = false }
            )
        }

        if (showSettings) {
            DearFamilySettingsDialog(
                currentPersona = state.persona,
                themeConfig = state.themeConfig,
                onPersonaChange = { vm.setPersona(it) },
                onThemeConfigChange = { d, p -> vm.updateThemeConfig(d, p) },
                onDismiss = onCloseSettings
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    isDriving: Boolean,
    onToggleDriveMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenJourney: () -> Unit,
    onOpenDebug: () -> Unit
) {
    val palette = LocalThemePalette.current
    var menu by remember { mutableStateOf(false) }
    val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    val controls: @Composable () -> Unit = {
        TextButton(onToggleDriveMode, Modifier.heightIn(min = 48.dp).testTag("drivingModeToggle")) {
            Text(if (isDriving) "모의 주행" else "정차", style = MaterialTheme.typography.labelSmall)
        }
        Box {
            TextButton({ menu = true }, Modifier.heightIn(min = 48.dp).testTag("themePicker")) {
                Text(when (themeMode) {
                    ThemeMode.GENESIS_LUXURY -> "Luxury"
                    ThemeMode.SCANDINAVIAN_WARM -> "Warm"
                    ThemeMode.SPATIAL_WIDGET -> "Spatial"
                }, style = MaterialTheme.typography.labelSmall)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                ThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.label) },
                        onClick = { onThemeChange(mode); menu = false },
                        modifier = Modifier.testTag("theme_${mode.name}")
                    )
                }
            }
        }
        TextButton(onOpenSettings, Modifier.heightIn(min = 48.dp).testTag("settingsButton")) {
            Text("설정", style = MaterialTheme.typography.labelSmall)
        }
        TextButton(onOpenJourney, Modifier.heightIn(min = 48.dp).testTag("openFamilyJourney")) {
            Text("가족 여정", style = MaterialTheme.typography.labelSmall)
        }
        Surface(
            onClick = onOpenDebug,
            modifier = Modifier.heightIn(min = 48.dp).testTag("debugBadge"),
            shape = RoundedCornerShape(50),
            color = palette.cardBackgroundElevated.copy(alpha = .75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
        ) {
            Text("Mock / Debug", Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelSmall, color = palette.accentSecondary)
        }
    }
    val title: @Composable () -> Unit = {
        Column {
            Text("DEAR FAMILY", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            Text("케어 MOCK · 차량 조회 READ-ONLY", style = MaterialTheme.typography.labelSmall,
                color = palette.secondaryText, modifier = Modifier.testTag("simulationNotice"))
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 820.dp || fontScale > 1.2f) {
            Column(Modifier.fillMaxWidth()) {
                title()
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) { controls() }
            }
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { title() }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) { controls() }
            }
        }
    }
}

@Composable
private fun DebugOverlay(
    state: FamilyCareUiState,
    vehicle: VehicleReadState,
    narrator: AudioNarrator,
    vm: DearFamilyViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val palette = LocalThemePalette.current
        BoxWithConstraints(
            Modifier.fillMaxWidth(.94f).fillMaxHeight(.9f)
        ) {
            val panelHeight = maxHeight
            Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            color = palette.background,
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
        ) {
            Column(
                Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Mock / Debug", style = MaterialTheme.typography.titleLarge, color = palette.accentPrimary)
                        Text("가족 케어는 MOCK · 차량 조회는 READ-ONLY입니다.", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("closeDebug")) { Text("닫기") }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.cardBackground,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("✨ AI 동반자 · ${state.persona.title}", style = MaterialTheme.typography.titleMedium, color = palette.accentSecondary)
                        Text(state.companionMessage, style = MaterialTheme.typography.bodyMedium)
                        Text("AI 호출 없음 · 가족 케어 MOCK 시나리오", style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                    }
                }
                ScenarioInjectorDock(
                    state = state,
                    onScenario = vm::manual,
                    onTour = vm::startTour,
                    onStop = vm::stopTour,
                    onReset = vm::reset,
                    onNoise = vm::setNoise,
                    onElapsed = vm::setElapsed,
                    compact = false,
                    initiallyExpanded = true,
                    modifier = Modifier.testTag("debugDock")
                )
                IpoPipelineMonitor(state.flow, state.eventId, Modifier.heightIn(max = panelHeight * .34f))
                MultiZoneControllerPanel(
                    state = state,
                    voice = narrator.status.collectAsStateWithLifecycle().value,
                    onMute = narrator::toggleMute,
                    onToggleFun = vm::toggleMode,
                    onSnack = vm::chooseSnack,
                    onEndConversation = vm::endConversation,
                    onRestStop = vm::answerRestStop,
                    modifier = Modifier.fillMaxWidth()
                )
                VehicleReadout(vehicle, vm::refreshVehicle, Modifier.heightIn(max = panelHeight * .34f))
            }
            }
        }
    }
}

@Composable
private fun VehicleReadout(state: VehicleReadState, refresh: (() -> Unit)?, modifier: Modifier = Modifier) {
    val palette = LocalThemePalette.current
    Column(
        modifier
            .fillMaxWidth()
            .testTag("debugVehicle")
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("VEHICLE READ-ONLY · ${state.message}", color = palette.secondaryText, style = MaterialTheme.typography.bodySmall)
        state.occupancy.forEach { (area, occupied) ->
            Text(
                "$area: ${when (occupied) { true -> "탑승"; false -> "비어 있음"; null -> "알 수 없음" }}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.accentPrimary
            )
        }
        refresh?.let {
            OutlinedButton(it, Modifier.testTag("refreshVehicle")) {
                Text("실제 SDK 다시 조회")
            }
        }
    }
}
