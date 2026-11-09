package com.pleos.dearfamily.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
fun DearFamilyMainScreen(narrator: AudioNarrator, vm: DearFamilyViewModel = viewModel()) {
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
                    "모든 가족 케어 제어는 시뮬레이션입니다.",
                    color = SecondaryText,
                    modifier = Modifier.testTag("simulationNotice")
                )
                if (safety.demoBuild && !safety.automotive) {
                    Button(vm::enterDemo, Modifier.testTag("enterDemo")) {
                        Text("정차 모의 시연 시작")
                    }
                }
                VehicleReadout(vehicle, null)
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

    var showThemeMenu by remember { mutableStateOf(false) }

    Box(
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
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 1100.dp && maxHeight >= 650.dp
            val compactDock = maxHeight < 560.dp

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Global Bar with Theme Picker & Driving Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (wide) {
                        TopPeaceShieldBar(Modifier.weight(0.6f))
                    } else {
                        Column(Modifier.weight(0.6f)) {
                            Text("DEAR FAMILY · 정차 시연", color = palette.accentPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "가족 케어는 MOCK · 실제 차량 제어 없음",
                                color = palette.secondaryText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag("simulationNotice")
                            )
                        }
                    }

                    // Interactive Control Capsule in Header
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drive Mode Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.cardBackgroundElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier
                                .clickable { onToggleDriveMode() }
                                .testTag("drivingModeToggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isDriving) palette.accentPrimary else palette.accentSecondary)
                                )
                                Text(
                                    if (isDriving) "주행 중 (Driving)" else "정차/휴식 (Parked)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primaryText
                                )
                            }
                        }

                        // Theme Switcher Button with Dropdown
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = palette.cardBackgroundElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { showThemeMenu = true }
                                    .testTag("themePicker")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎨 " + themeMode.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = palette.accentPrimary)
                                }
                            }

                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(mode.label, fontWeight = FontWeight.Bold)
                                                Text(mode.subtitle, style = MaterialTheme.typography.bodySmall, color = palette.secondaryText)
                                            }
                                        },
                                        onClick = {
                                            onThemeChange(mode)
                                            showThemeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Settings Icon Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.cardBackgroundElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier
                                .clickable { onOpenSettings() }
                                .testTag("settingsButton")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚙️ 설정", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = palette.primaryText)
                            }
                        }
                    }
                }

                // AI Invisible Companion Floating Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardBackgroundElevated.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentSecondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aiCompanionBanner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.accentSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "✨ AI 동반자 · ${state.persona.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            state.companionMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.primaryText,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.cardBackground,
                            modifier = Modifier.clickable { onOpenSettings() }
                        ) {
                            Text(
                                "케어 프로필 ⚙️",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = palette.secondaryText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
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
                                onMute = narrator::toggleMute,
                                onToggleFun = vm::toggleMode,
                                onSnack = vm::chooseSnack,
                                onEndConversation = vm::endConversation,
                                onRestStop = vm::answerRestStop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        themeMode == ThemeMode.SPATIAL_WIDGET -> {
                            SpatialDynamicCardsView(
                                state = state,
                                voice = voice,
                                onMute = narrator::toggleMute,
                                onToggleFun = vm::toggleMode,
                                onSnack = vm::chooseSnack,
                                onEndConversation = vm::endConversation,
                                onRestStop = vm::answerRestStop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            // GENESIS_LUXURY (Default flagship ccNC Cockpit)
                            GenesisLuxuryCockpitView(
                                state = state,
                                voice = voice,
                                onMute = narrator::toggleMute,
                                onToggleFun = vm::toggleMode,
                                onSnack = vm::chooseSnack,
                                onEndConversation = vm::endConversation,
                                onRestStop = vm::answerRestStop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (vehicle.status != com.pleos.dearfamily.platform.VehicleLinkStatus.MOCK_ONLY) {
                    VehicleReadout(vehicle, vm::refreshVehicle)
                }

                // Retractable Scenario Injector Dock (Always present and accessible)
                ScenarioInjectorDock(
                    state = state,
                    onScenario = vm::manual,
                    onTour = vm::startTour,
                    onStop = vm::stopTour,
                    onReset = vm::reset,
                    onNoise = vm::setNoise,
                    onElapsed = vm::setElapsed,
                    compact = compactDock
                )
            }
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
private fun VehicleReadout(state: VehicleReadState, refresh: (() -> Unit)?) {
    val palette = LocalThemePalette.current
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 125.dp)
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
