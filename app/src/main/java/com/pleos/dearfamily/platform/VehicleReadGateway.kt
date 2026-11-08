package com.pleos.dearfamily.platform

import kotlinx.coroutines.flow.StateFlow

enum class VehicleLinkStatus { MOCK_ONLY, CONNECTING, READY, UNAVAILABLE, ERROR }
data class VehicleReadState(
    val status: VehicleLinkStatus = VehicleLinkStatus.MOCK_ONLY,
    val message: String = "실제 SDK 연결 없음 · 모의 센서/제어",
    val access: String = "NONE",
    val occupancy: Map<String, Boolean?> = emptyMap()
)
interface VehicleReadGateway {
    val demoOnly: Boolean
    val state: StateFlow<VehicleReadState>
    fun start()
    fun refresh()
    fun stop()
}
