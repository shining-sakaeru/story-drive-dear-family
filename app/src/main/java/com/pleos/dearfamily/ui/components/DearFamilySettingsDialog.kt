package com.pleos.dearfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pleos.dearfamily.model.DriveModeThemeConfig
import com.pleos.dearfamily.model.FamilyPersona
import com.pleos.dearfamily.ui.theme.LocalThemePalette
import com.pleos.dearfamily.ui.theme.ThemeMode

@Composable
fun DearFamilySettingsDialog(
    currentPersona: FamilyPersona,
    themeConfig: DriveModeThemeConfig,
    onPersonaChange: (FamilyPersona) -> Unit,
    onThemeConfigChange: (driving: String, parked: String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current
    var drivingTheme by remember { mutableStateOf(themeConfig.drivingThemeName) }
    var parkedTheme by remember { mutableStateOf(themeConfig.parkedThemeName) }
    var selectedPersona by remember { mutableStateOf(currentPersona) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.accentPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .testTag("settingsDialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "⚙️ 여정 & 테마 환경설정",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.primaryText
                        )
                        Text(
                            "주행 모드별 자동 테마 매핑 및 탑승 가족 맞춤 AI 케어 프로필",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("closeSettingsDialog")
                    ) {
                        Text("✕", fontSize = 20.sp, color = palette.primaryText, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = palette.cardBorder, thickness = 1.dp)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Section 1: Driving Mode to Theme Mapping
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "🎨 주행 모드별 기본 테마 자동 매핑",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentPrimary
                        )
                        Text(
                            "차량 주행 상태 전환 시 화면이 최적의 테마로 자동 전환됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )

                        // 1-1: Driving Mode Theme
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.cardBackgroundElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🚗 주행 중 (Driving Mode) 테마", fontWeight = FontWeight.Bold, color = palette.primaryText)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemeMode.entries.forEach { mode ->
                                        val isSelected = drivingTheme == mode.name
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) palette.accentPrimary.copy(alpha = 0.2f) else palette.cardBackground,
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) palette.accentPrimary else palette.cardBorder
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    drivingTheme = mode.name
                                                    onThemeConfigChange(mode.name, parkedTheme)
                                                }
                                                .testTag("settingDrivingTheme_${mode.name}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    mode.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) palette.accentPrimary else palette.secondaryText
                                                )
                                                Text(
                                                    mode.subtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
                                                    color = palette.secondaryText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 1-2: Parked / Lounge Mode Theme
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.cardBackgroundElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🛋️ 정차 / 휴식 (Parked Lounge) 테마", fontWeight = FontWeight.Bold, color = palette.primaryText)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemeMode.entries.forEach { mode ->
                                        val isSelected = parkedTheme == mode.name
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) palette.accentWarm.copy(alpha = 0.2f) else palette.cardBackground,
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) palette.accentWarm else palette.cardBorder
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    parkedTheme = mode.name
                                                    onThemeConfigChange(drivingTheme, mode.name)
                                                }
                                                .testTag("settingParkedTheme_${mode.name}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    mode.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) palette.accentWarm else palette.secondaryText
                                                )
                                                Text(
                                                    mode.subtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
                                                    color = palette.secondaryText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Family Demographics & Companion Persona
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "👥 탑승 가족 맞춤 AI 컴패니언 페르소나",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentSecondary
                        )
                        Text(
                            "2열 및 전 좌석 탑승객의 연령·상황에 맞춰 AI 대화 톤과 차량 제어가 적응합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )

                        FamilyPersona.entries.forEach { persona ->
                            val isSelected = selectedPersona == persona
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) palette.cardBackgroundElevated else palette.cardBackgroundElevated.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) palette.accentSecondary else palette.cardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPersona = persona
                                        onPersonaChange(persona)
                                    }
                                    .testTag("settingPersona_${persona.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                persona.title,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.primaryText
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = palette.accentSecondary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    persona.ageGroup,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.accentSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            persona.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.secondaryText
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedPersona = persona
                                            onPersonaChange(persona)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = palette.accentSecondary)
                                    )
                                }
                            }
                        }
                    }

                    // Section 3: Safety & Audio Policy
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackgroundElevated.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🛡️ 주행 안전 및 오디오 분리 준수", fontWeight = FontWeight.Bold, color = palette.primaryText, style = MaterialTheme.typography.labelMedium)
                            Text(
                                "• CarUxRestrictions 주행 규제 적용: 주행 중 산만 요소를 자동 차단하고 시인성을 극대화합니다.\n• AudioFocus 및 TTS 선행 묵음: 내비게이션 안내 우선권을 보장하며, 다중 존 제어는 모의 시뮬레이션입니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("saveSettingsButton")
                    ) {
                        Text("설정 저장 및 닫기", fontWeight = FontWeight.Bold, color = palette.background)
                    }
                }
            }
        }
    }
}
