package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.R
import com.pleos.dearfamily.model.*
import com.pleos.dearfamily.platform.VoiceStatus

private val GenesisGold = Color(0xFFC5A070)
private val GenesisSilver = Color(0xFFB7C0CA)
private val GlassSurface = Color(0xB314171F)
private const val CAR_ASPECT = 567f / 387f

/** Positions come from the reference scene, including its card/vehicle occlusions. */
@Composable
fun GenesisLuxuryCockpitView(
    state: FamilyCareUiState, voice: VoiceStatus, onMute: () -> Unit,
    onStoryPlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedZone by remember { mutableStateOf(SeatZone.DRIVER_1L) }
    BoxWithConstraints(modifier.fillMaxSize().testTag("genesisContent")
        .background(Brush.radialGradient(listOf(Color(0xFF252D34), Color(0xFF090D12))))) {
        val viewportHeight = maxHeight
        val stageHeight = maxHeight * .85f
        val narrow = maxWidth < 900.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
        // Source scene (215,142)-(1161,586): one uniform transform for art AND cards.
        val scale = minOf(maxWidth.value / 946f, stageHeight.value / 444f)
        val sceneLeft = (maxWidth - (946f * scale).dp) / 2
        val sceneTop = (stageHeight - (444f * scale).dp) / 2
        fun slot(x: Float, y: Float, w: Float, h: Float): Modifier =
            Modifier.offset(sceneLeft + (x * scale).dp, sceneTop + (y * scale).dp)
                .size((w * scale).dp, (h * scale).dp)
        val choose: (SeatZone) -> Unit = { selectedZone = it }
        if (narrow) {
            Column(Modifier.fillMaxWidth().height(stageHeight).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GenesisVehicle(state, selectedZone, choose,
                    Modifier.fillMaxWidth().height(viewportHeight * .3f))
                Text("좌석 케어를 아래로 스크롤해 확인하세요", fontSize = 14.sp, color = GenesisSilver)
                state.seats.forEach { seat ->
                    GenesisSeatCard(state, seat, voice, selectedZone, choose, onMute, onStoryPlayback,
                        Modifier.fillMaxWidth().heightIn(max = viewportHeight * .28f))
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(stageHeight)) {
                GenesisVehicle(state, selectedZone, choose, slot(195f, 51f, 567f, 387f))
                GenesisSeatCard(state, state.seats.first { it.zone == SeatZone.DRIVER_1L }, voice,
                    selectedZone, choose, onMute, onStoryPlayback, slot(45f, 29f, 238f, 111f))
                GenesisSeatCard(state, state.seats.first { it.zone == SeatZone.PASSENGER_1R }, voice,
                    selectedZone, choose, onMute, onStoryPlayback, slot(671f, 28f, 240f, 172f))
                GenesisSeatCard(state, state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }, voice,
                    selectedZone, choose, onMute, onStoryPlayback, slot(45f, 268f, 238f, 150f))
                GenesisSeatCard(state, state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }, voice,
                    selectedZone, choose, onMute, onStoryPlayback, slot(671f, 268f, 240f, 150f))
            }
        }
        Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(.94f).height(maxHeight * .12f),
            shape = RoundedCornerShape(24.dp), color = GlassSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GenesisSilver.copy(alpha = .5f))) {
            Row(Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SeatZone.entries.forEach { zone ->
                    val active = selectedZone == zone
                    Surface(onClick = { choose(zone) }, modifier = Modifier.weight(1f)
                        .heightIn(min = 48.dp).testTag("zoneButton_${zone.name}")
                        .semantics { selected = active }, shape = RoundedCornerShape(20.dp),
                        color = if (active) GenesisGold.copy(alpha = .25f) else Color.Transparent) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(zone.label, fontSize = 14.sp, color = if (active) GenesisGold else GenesisSilver)
                            Text("${state.seats.first { it.zone == zone }.temperature}°C", fontSize = 24.sp,
                                color = Color(0xFFF0EDE5))
                        }
                    }
                }
                IconButton(onMute, Modifier.size(48.dp).testTag("genesisMute")
                    .semantics { selected = voice.muted }) {
                    Icon(painterResource(R.drawable.ic_spatial_quick_mute),
                        if (voice.muted) "앱 음성 음소거 해제" else "앱 음성 음소거",
                        tint = if (voice.muted) GenesisGold else GenesisSilver)
                }
            }
        }
    }
}

@Composable
private fun GenesisVehicle(state: FamilyCareUiState, selectedZone: SeatZone,
    onSelect: (SeatZone) -> Unit, modifier: Modifier) {
    BoxWithConstraints(modifier) {
        val imageWidth = minOf(maxWidth, maxHeight * CAR_ASPECT)
        val imageHeight = imageWidth / CAR_ASPECT
        Box(Modifier.align(Alignment.Center).size(imageWidth, imageHeight).testTag("genesisVehicleArtwork")) {
            Image(painterResource(R.drawable.img_genesis_car_base), "사선 차량 컷어웨이 참고 렌더",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            val points = listOf(.42f to .49f, .55f to .28f, .65f to .60f, .78f to .42f)
            state.seats.forEach { seat ->
                val point = points[seat.zone.ordinal]
                Box(Modifier.offset(imageWidth * point.first - 24.dp, imageHeight * point.second - 24.dp)
                    .size(48.dp).clickable { onSelect(seat.zone) }.testTag("seatMarker_${seat.zone.name}")
                    .semantics { selected = selectedZone == seat.zone }, contentAlignment = Alignment.Center) {
                    if (seat.active) Image(painterResource(R.drawable.img_genesis_seat_glow), null,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    Surface(shape = CircleShape, color = if (selectedZone == seat.zone) GenesisGold else GlassSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GenesisSilver)) { Box(Modifier.size(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun GenesisSeatCard(state: FamilyCareUiState, seat: SeatStatus, voice: VoiceStatus,
    selectedZone: SeatZone, onSelect: (SeatZone) -> Unit,
    onMute: () -> Unit, onStoryPlayback: () -> Unit, modifier: Modifier) {
    val active = selectedZone == seat.zone || seat.active
    val title = when (seat.zone) {
        SeatZone.DRIVER_1L -> "Driver Peace Zone"
        SeatZone.PASSENGER_1R -> "Front Passenger Comfort"
        SeatZone.KIDS_2ND_ROW -> "Kids Dream Story"
        SeatZone.PARENTS_3RD_ROW -> "Rear Seat Comfort"
    }
    val status = when (seat.zone) {
        SeatZone.DRIVER_1L -> if (voice.muted) "앱 음성 음소거" else if (voice.ready) "오프라인 음성" else "자막 안내"
        SeatZone.KIDS_2ND_ROW -> when { !seat.ambientOn -> "조명 OFF · 수면 보호"; seat.freshAir -> "외기 순환 · 케어"; else -> seat.status }
        SeatZone.PARENTS_3RD_ROW -> if (seat.heatingLevel > 0) "온열 ${seat.heatingLevel} · 요추 ${if (seat.lumbarOn) "ON" else "OFF"}" else seat.status
        else -> seat.status
    }
    Surface(modifier.clickable { onSelect(seat.zone) }.testTag("seat_${seat.zone.name}"),
        shape = RoundedCornerShape(20.dp), color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(
            listOf(if (active) GenesisGold else GenesisSilver.copy(alpha = .45f), GenesisSilver.copy(alpha = .3f)))),
        shadowElevation = 8.dp) {
        Column(Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, color = GenesisGold)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(status, fontSize = 16.sp, lineHeight = 22.sp, color = GenesisSilver)
                when (seat.zone) {
                    SeatZone.DRIVER_1L -> Unit
                    SeatZone.PASSENGER_1R -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painter = painterResource(R.drawable.img_genesis_album), contentDescription = "원본 앨범 참고 이미지",
                            modifier = Modifier.size(96.dp), contentScale = ContentScale.Fit)
                        Column(Modifier.weight(1f)) {
                            Text("별빛 여정", fontSize = 24.sp, lineHeight = 28.sp, color = Color(0xFFF0EDE5))
                            Text("오프라인 동화 시연", fontSize = 16.sp, lineHeight = 22.sp, color = GenesisSilver)
                        }
                    }
                    SeatZone.KIDS_2ND_ROW -> Image(painter = painterResource(R.drawable.img_genesis_waveform),
                        contentDescription = "원본 모의 파형 · 측정 오디오 아님", modifier = Modifier.fillMaxWidth().height(80.dp), contentScale = ContentScale.Fit)
                    SeatZone.PARENTS_3RD_ROW -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(painterResource(R.drawable.ic_spatial_seat_heating), null, tint = GenesisGold, modifier = Modifier.size(32.dp))
                        Text("온열 ${seat.heatingLevel}/3 · 팬 ${seat.fanLevel}/3 · MOCK", fontSize = 16.sp, lineHeight = 22.sp, color = GenesisSilver)
                    }
                }
                val volume = state.audio.first { it.zone == seat.zone }.volume
                val metric = if (seat.zone == SeatZone.PARENTS_3RD_ROW) "${seat.temperature}°C" else "$volume%"
                Text(metric, fontSize = 32.sp, lineHeight = 36.sp, color = Color(0xFFF0EDE5),
                    modifier = Modifier.testTag("genesisMetric_${seat.zone.name}"))
                if (seat.zone != SeatZone.PARENTS_3RD_ROW) {
                    Text(if (seat.zone == SeatZone.DRIVER_1L) "MOCK 오디오 · 실제 ANC 아님" else "MOCK 오디오",
                        fontSize = 14.sp, lineHeight = 20.sp, color = GenesisSilver)
                    Box(Modifier.fillMaxWidth().height(6.dp).background(GenesisSilver.copy(alpha = .2f), RoundedCornerShape(3.dp))) {
                        Box(Modifier.fillMaxWidth((volume / 100f).coerceIn(0f, 1f)).fillMaxHeight().background(GenesisGold, RoundedCornerShape(3.dp)))
                    }
                }
                if (seat.zone == SeatZone.PASSENGER_1R && state.storyPlaying) {
                    Text(state.flow.gleoTtsScript, fontSize = 16.sp, lineHeight = 22.sp,
                        color = GenesisSilver, modifier = Modifier.testTag("genesisStoryCaption"))
                }
            }
            if (seat.zone == SeatZone.DRIVER_1L) {
                OutlinedButton(onMute, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("genesisCardMute")) {
                    Text(if (voice.muted) "앱 음성 켜기" else "앱 음성 음소거", fontSize = 16.sp, color = GenesisGold)
                }
            } else if (seat.zone == SeatZone.PASSENGER_1R) {
                OutlinedButton(onStoryPlayback, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("genesisStory"),
                    enabled = !state.quietCabin && state.silentMessage == null) {
                    Text(if (state.storyPlaying) "동화 중지" else "동화 시연 재생", fontSize = 16.sp, color = GenesisGold)
                }
            }
        }
    }
}
