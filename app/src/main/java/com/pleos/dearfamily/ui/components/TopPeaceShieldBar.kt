package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.ui.theme.*

@Composable fun TopPeaceShieldBar(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Emerald))
                Text("DEAR FAMILY", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold)
                Text("STORY DRIVE", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
            }
            Text("Pleos Connect · 모든 센서·차량 제어·음장 분리는 시뮬레이션",
                color = SecondaryText, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("simulationNotice"))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("DRIVING  85 km/h", color = PrimaryText, style = MaterialTheme.typography.bodyMedium)
            Text("모의 주행 데이터", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("PEACE SHIELD", color = Emerald, style = MaterialTheme.typography.labelSmall)
            Text("운전자 스트레스 0%", color = Emerald, style = MaterialTheme.typography.titleMedium)
            Text("데모 지표 · 실제 측정 아님", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
        }
    }
}
