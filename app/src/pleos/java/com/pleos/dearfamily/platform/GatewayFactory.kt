package com.pleos.dearfamily.platform

import android.content.Context
import android.content.pm.PackageManager
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.Seat
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.capability.AccessType
import ai.pleos.playground.vehicle.api.model.capability.CapabilityConfig
import ai.pleos.playground.vehicle.listener.SeatOccupancyListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object GatewayFactory {
    fun create(context: Context, scope: CoroutineScope): VehicleReadGateway = PleosSeatGateway(context.applicationContext, scope)
}

/** Vehicle 2.3.2 signatures/permission were inspected in the published AAR. No set/control APIs. */
private class PleosSeatGateway(private val context: Context, private val scope: CoroutineScope) : VehicleReadGateway {
    override val demoOnly = false
    private val mutable = MutableStateFlow(VehicleReadState(status = VehicleLinkStatus.UNAVAILABLE,
        message = "Pleos Vehicle SDK · 아직 연결하지 않음"))
    override val state = mutable.asStateFlow()
    private var vehicle: Vehicle? = null
    private var seat: Seat? = null
    private var epoch = 0L
    private var deadline: Job? = null
    private val subscriptions = mutableListOf<Pair<SeatArea, SeatOccupancyListener>>()

    override fun start() {
        if (vehicle != null) return
        if (context.checkSelfPermission("pleos.car.permission.CAR_SEATS") != PackageManager.PERMISSION_GRANTED) {
            mutable.value = VehicleReadState(VehicleLinkStatus.UNAVAILABLE,
                "CAR_SEATS 조회 권한 없음 · 승인/플랫폼 권한 확인 필요")
            return
        }
        try {
            val sdk = Vehicle(context)
            vehicle = sdk
            sdk.initialize()
            seat = sdk.getSeat()
            refresh()
        } catch (e: Exception) { fail("SDK 초기화 실패: ${e.javaClass.simpleName}"); stopConnection() }
        catch (_: LinkageError) { fail("Vehicle SDK 플랫폼 서비스 사용 불가"); stopConnection() }
    }

    override fun refresh() {
        if (vehicle == null) { start(); return }
        val domain = seat ?: return
        val version = ++epoch
        unregister()
        mutable.value = VehicleReadState(VehicleLinkStatus.CONNECTING, "좌석 탑승 Capability 확인 중")
        deadline?.cancel()
        deadline = scope.launch {
            delay(10_000)
            if (version == epoch && mutable.value.status == VehicleLinkStatus.CONNECTING)
                fail("Vehicle SDK 응답 시간 초과 · 에뮬레이터/권한 확인")
        }
        try {
            domain.checkSeatOccupancyCapabilities(
                onComplete = { config: CapabilityConfig, areas: List<SeatArea> ->
                    scope.launch {
                        if (version != epoch) return@launch
                        if (config.accessType != AccessType.READ && config.accessType != AccessType.READ_WRITE) {
                            deadline?.cancel()
                            mutable.value = VehicleReadState(VehicleLinkStatus.UNAVAILABLE,
                                "좌석 탑승 읽기 미지원", config.accessType.name)
                            return@launch
                        }
                        if (areas.isEmpty()) {
                            deadline?.cancel()
                            mutable.value = VehicleReadState(VehicleLinkStatus.UNAVAILABLE,
                                "지원되는 좌석 영역 없음", config.accessType.name)
                            return@launch
                        }
                        mutable.value = VehicleReadState(VehicleLinkStatus.CONNECTING,
                            "지원 좌석 조회/구독 중", config.accessType.name,
                            areas.associate { label(it) to null })
                        areas.forEach { area -> observe(domain, area, version) }
                    }
                },
                onFailed = { e -> scope.launch { if (version == epoch) fail("Capability 조회 실패: ${e.javaClass.simpleName}") } }
            )
        } catch (e: Exception) { fail("Capability 호출 실패: ${e.javaClass.simpleName}") }
    }

    private fun observe(domain: Seat, area: SeatArea, version: Long) {
        val listener = object : SeatOccupancyListener {
            override fun onSeatOccupancyUpdated(occupancy: Boolean?) { accept(area, occupancy, version) }
            override fun onFailed(e: Exception) {
                scope.launch { if (version == epoch) fail("좌석 구독 실패: ${e.javaClass.simpleName}") }
            }
        }
        subscriptions += area to listener
        try {
            domain.registerSeatOccupancy(area, listener)
            domain.getCurrentSeatOccupancy(area = area,
                onComplete = { occupied -> accept(area, occupied, version) },
                onFailed = { e -> scope.launch { if (version == epoch) fail("좌석 조회 실패: ${e.javaClass.simpleName}") } })
        } catch (e: Exception) { fail("좌석 API 호출 실패: ${e.javaClass.simpleName}") }
    }
    private fun accept(area: SeatArea, occupied: Boolean?, version: Long) {
        scope.launch {
            if (version != epoch) return@launch
            deadline?.cancel()
            mutable.update { it.copy(status = VehicleLinkStatus.READY,
                message = "실제 Vehicle SDK 탑승 상태 · 차량 제어는 MOCK",
                occupancy = it.occupancy + (label(area) to occupied)) }
        }
    }
    private fun label(area: SeatArea) = area.values.joinToString("+") { it.name }
    private fun fail(message: String) {
        deadline?.cancel()
        mutable.update { it.copy(status = VehicleLinkStatus.ERROR, message = message) }
    }
    private fun unregister() {
        subscriptions.forEach { (area, listener) -> runCatching { seat?.unregisterSeatOccupancy(area, listener) } }
        subscriptions.clear()
    }
    private fun stopConnection() {
        unregister()
        runCatching { vehicle?.release() }
        vehicle = null; seat = null
    }
    override fun stop() {
        ++epoch
        deadline?.cancel(); deadline = null
        stopConnection()
        mutable.update { it.copy(status = VehicleLinkStatus.UNAVAILABLE, message = "SDK 연결 해제 · 백그라운드 보호") }
    }
}
