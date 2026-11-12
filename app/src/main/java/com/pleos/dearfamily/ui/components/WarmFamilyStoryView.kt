package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pleos.dearfamily.R
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.model.SeatStatus
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.platform.VoiceStatus

private val Espresso = Color(0xFF2A201B)
private val Sage = Color(0xFF7D9B8A)
private val Cream = Color(0xFFF5EBE1)
private val Amber = Color(0xFFF2C879)

@Composable
fun WarmFamilyStoryView(
    state: FamilyCareUiState,
    voice: VoiceStatus,
    onStoryPlayback: () -> Unit,
    onCabinWarmth: () -> Unit,
    onQuietCabin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kids = state.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
    val driver = state.seats.first { it.zone == SeatZone.DRIVER_1L }
    val passenger = state.seats.first { it.zone == SeatZone.PASSENGER_1R }
    val parents = state.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }

    BoxWithConstraints(
        modifier.fillMaxSize().testTag("warmContent").background(Brush.verticalGradient(listOf(Espresso, Color(0xFF4B594B))))
    ) {
        val narrow = maxWidth <= 780.dp
        val narrowCardWidth = ((maxWidth - 48.dp).coerceAtLeast(0.dp) * .9f).coerceAtLeast(240.dp)
        val cards: @Composable RowScope.() -> Unit = {
            WarmCard(
                zone = SeatZone.KIDS_2ND_ROW,
                modifier = if (narrow) Modifier.width(narrowCardWidth) else Modifier.weight(1f),
                title = "Kids Dream Story",
                art = R.drawable.img_warm_fox,
                seat = kids,
                metadata = {
                    Text("별빛 아래 잠든 여우의 이야기", style = MaterialTheme.typography.bodyLarge, color = Cream)
                    Text(kidsStatus(kids), style = MaterialTheme.typography.bodyMedium, color = Amber)
                    Text("${state.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }.volume}% · MOCK 오디오", style = MaterialTheme.typography.bodySmall, color = Cream)
                    if (state.storyPlaying && !voice.ready) {
                        Text("음성 준비 안 됨 · 자막 시연", style = MaterialTheme.typography.bodySmall, color = Amber)
                    }
                    if (state.storyPlaying) {
                        Text(state.flow.gleoTtsScript, style = MaterialTheme.typography.bodyMedium,
                            color = Cream, modifier = Modifier.testTag("warmStoryCaption"))
                    }
                },
                action = {
                    WarmPill(
                        label = if (state.storyPlaying) "Stop Story" else "Play Story",
                        tag = "playStory",
                        enabled = !state.quietCabin && state.silentMessage == null,
                        isSelected = state.storyPlaying,
                        onClick = onStoryPlayback
                    )
                }
            )
            WarmCard(
                zone = SeatZone.DRIVER_1L,
                modifier = if (narrow) Modifier.width(narrowCardWidth) else Modifier.weight(1f),
                title = "Family Harmony",
                art = R.drawable.img_warm_cabin,
                seat = driver,
                metadata = {
                    Text(if (state.cabinWarmth) "Fireside Amber · 22.5°C" else "Night Sky · 22.0°C", style = MaterialTheme.typography.bodyLarge, color = Amber)
                    Text("모의 화목지수 · 98%", style = MaterialTheme.typography.bodySmall, color = Cream)
                    Text("운전석 ${driver.status} · ${driver.temperature}°C", style = MaterialTheme.typography.bodySmall, color = Cream, modifier = Modifier)
                    Box(Modifier.testTag("seat_PASSENGER_1R")) {
                        Text("조수석 ${passenger.status} · ${passenger.temperature}°C", style = MaterialTheme.typography.bodySmall, color = Cream)
                    }
                },
                action = {
                    WarmPill(
                        label = if (state.cabinWarmth) "Cabin Warmth On" else "Cabin Warmth Off",
                        tag = "cabinWarmth",
                        isSelected = state.cabinWarmth,
                        onClick = onCabinWarmth
                    )
                }
            )
            WarmCard(
                zone = SeatZone.PARENTS_3RD_ROW,
                modifier = if (narrow) Modifier.width(narrowCardWidth) else Modifier.weight(1f),
                title = "Seat Comfort & Quiet",
                art = R.drawable.img_warm_seat,
                seat = parents,
                metadata = {
                    Text(parents.status, style = MaterialTheme.typography.bodyLarge, color = Cream, modifier = Modifier)
                    Text(parentsStatus(parents), style = MaterialTheme.typography.bodyMedium, color = Amber)
                    Text("실제 마사지·ANC 제어 아님 · MOCK", style = MaterialTheme.typography.bodySmall, color = Cream)
                },
                action = {
                    WarmPill(
                        label = if (state.quietCabin) "Quiet Cabin On" else "Quiet Cabin Off",
                        tag = "quietCabin",
                        isSelected = state.quietCabin,
                        onClick = onQuietCabin
                    )
                }
            )
        }

        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                Modifier.fillMaxWidth().weight(1f).then(if (narrow) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                content = cards
            )
        }
    }
}

@Composable
private fun WarmCard(
    zone: SeatZone,
    modifier: Modifier,
    title: String,
    art: Int,
    seat: SeatStatus,
    metadata: @Composable ColumnScope.() -> Unit,
    action: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight().testTag("seat_${zone.name}"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xCC30251F),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (seat.active) Sage else Sage.copy(alpha = .35f)),
        shadowElevation = 8.dp
    ) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                painter = painterResource(art),
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().weight(.57f).clip(RoundedCornerShape(18.dp)),
                contentScale = if (art == R.drawable.img_warm_seat) ContentScale.Fit else ContentScale.Crop
            )
            Column(Modifier.weight(.43f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontSize = 26.sp, lineHeight = 30.sp, color = Cream, fontWeight = FontWeight.SemiBold)
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = metadata
                )
                Box(Modifier.fillMaxWidth().heightIn(min = 56.dp)) { action() }
            }
        }
    }
}

@Composable
private fun WarmPill(
    label: String,
    tag: String,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag)
            .semantics { this.selected = isSelected },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Sage.copy(alpha = .55f) else Sage,
            contentColor = Color.White,
            disabledContainerColor = Sage.copy(alpha = .35f)
        )
    ) { Text(label, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold) }
}

private fun kidsStatus(seat: SeatStatus) = when {
    !seat.ambientOn -> "조명 OFF · 수면 보호"
    seat.freshAir -> "외기 순환 · 동화 케어"
    else -> seat.status
}

private fun parentsStatus(seat: SeatStatus) = when {
    seat.heatingLevel > 0 -> "온열 ${seat.heatingLevel} · 요추 ${if (seat.lumbarOn) "ON" else "OFF"}"
    else -> seat.status
}
