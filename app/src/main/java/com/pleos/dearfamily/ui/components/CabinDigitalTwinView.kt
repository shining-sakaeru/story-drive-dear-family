package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.R
import com.pleos.dearfamily.model.FamilyCareUiState

/** Legacy read-only diagnostic preview; shares the real artwork, never a fake car Path. */
@Composable
fun CabinDigitalTwinView(state: FamilyCareUiState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(R.drawable.img_genesis_car_base), "차량 참고 렌더 · 모의 케어 존",
            modifier = Modifier.fillMaxWidth().weight(1f), contentScale = ContentScale.Fit)
        state.seats.forEach { seat ->
            Text("${seat.zone.label} · ${seat.status} · ${seat.temperature}°C (MOCK)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("seat_${seat.zone.name}"))
        }
    }
}
