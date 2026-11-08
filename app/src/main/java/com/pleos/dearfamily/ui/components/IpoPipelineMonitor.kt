package com.pleos.dearfamily.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.model.IpoFlowState
import com.pleos.dearfamily.ui.theme.*

@Composable fun IpoPipelineMonitor(flow: IpoFlowState, eventId: Long, modifier: Modifier = Modifier) {
    val color = Color(flow.targetZone.accent)
    val packet by rememberInfiniteTransition(label = "ipo").animateFloat(0f, 1f,
        infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "sensor packet")
    CarePanel(modifier) {
        PanelHeading("02", "배려가 시작되는 순간", "LIVE INPUT → PROCESSING → OUTPUT", color)
        Text(flow.activeScenarioTitle, color = color, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("activeScenario"))
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Stage("01", "INPUT", "Vehicle SDK · NaviHelper SDK", color) {
                if (flow.inputSensors.isEmpty()) Text("센서 입력 대기 · 아래 상황 버튼으로 시작", color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium)
                flow.inputSensors.forEach { sensor ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (sensor.isTriggered) "●" else "○", color = if (sensor.isTriggered) color else SecondaryText)
                        Column(Modifier.weight(1f)) {
                            Text(sensor.sensorKey, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                            Text(sensor.currentValue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${sensor.sdkName} · MOCK", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            PacketConnector(packet, color, eventId > 0)
            Stage("02", "PROCESSING", "Context Engine · LOCAL MOCK", color) {
                Text(flow.processingRule, style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("규칙 기반 컨텍스트 판단", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    Text("${flow.confidenceScore}%  MOCK", color = color, style = MaterialTheme.typography.labelSmall)
                }
            }
            PacketConnector((packet + .5f) % 1f, color, eventId > 0)
            Stage("03", "OUTPUT", "Multi-Zone Adapter · MOCK", color) {
                if (flow.vehicleControlOutputs.isEmpty()) Text("가족별 독립 케어 준비 완료", color = SecondaryText)
                flow.vehicleControlOutputs.forEach { Text("· $it", style = MaterialTheme.typography.bodyMedium) }
                Text(flow.audioRoutingOutput, color = color, style = MaterialTheme.typography.bodySmall)
                Text(flow.driverPeaceShieldStatus, color = Emerald, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(6.dp))
            Text("SDK 칩은 연동 구조 설명용입니다. 실제 차량 명령은 전송하지 않습니다.",
                style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        }
    }
}

@Composable private fun Stage(number: String, label: String, sdk: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(Background.copy(alpha = .6f), RoundedCornerShape(12.dp))
        .border(1.dp, color.copy(alpha = .22f), RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(number, color = color, style = MaterialTheme.typography.labelSmall)
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text("$sdk  · SIMULATED", color = SecondaryText, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.background(CardBorder.copy(alpha = .4f), RoundedCornerShape(5.dp)).padding(5.dp))
        content()
    }
}

@Composable private fun PacketConnector(progress: Float, color: Color, active: Boolean) {
    Canvas(Modifier.fillMaxWidth().height(25.dp)) {
        val x = size.width / 2
        drawLine(color.copy(alpha = .22f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        val y = size.height - 3.dp.toPx()
        drawLine(color.copy(alpha = .55f), Offset(x - 4.dp.toPx(), y - 4.dp.toPx()), Offset(x, y), 1.dp.toPx())
        drawLine(color.copy(alpha = .55f), Offset(x + 4.dp.toPx(), y - 4.dp.toPx()), Offset(x, y), 1.dp.toPx())
        if (active) {
            val point = Offset(x, progress * size.height)
            drawCircle(color.copy(alpha = .13f), 8.dp.toPx(), point)
            drawCircle(color.copy(alpha = .4f), 4.dp.toPx(), point)
            drawCircle(color, 2.dp.toPx(), point)
        }
    }
}
