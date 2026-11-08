package com.pleos.dearfamily.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.sdk.MockPleosSdkManager
import com.pleos.dearfamily.platform.CarUxObserver
import com.pleos.dearfamily.platform.GatewayFactory
import com.pleos.dearfamily.policy.SafetyState
import com.pleos.dearfamily.policy.SessionStopper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Android lifetime only; domain decisions and mock vehicle state live in the adapter. */
class DearFamilyViewModel(application: Application, private val saved: SavedStateHandle) : AndroidViewModel(application) {
    private val sdk = MockPleosSdkManager(viewModelScope)
    val state = sdk.state
    private val gateway = GatewayFactory.create(application, viewModelScope)
    val vehicle = gateway.state
    private val mutableSafety = MutableStateFlow(SafetyState(
        automotive = application.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE),
        demoBuild = gateway.demoOnly))
    val safety = mutableSafety.asStateFlow()
    private val ux = CarUxObserver(application) { known, needsOptimization, flags, message ->
        mutableSafety.update { it.copy(platformKnown = known, requiresOptimization = needsOptimization,
            activeRestrictions = flags, sourceMessage = message) }
        if (!mutableSafety.value.canInteract) sdk.shutdown()
    }
    private val stopper = SessionStopper(listOf(sdk::shutdown, ux::stop, gateway::stop))
    init {
        // Restore harmless UI preferences only. Never restore demo consent, a tour, or audio playback.
        if (saved.get<Boolean>("cozyMode") == false) sdk.toggleMode()
        saved.get<String>("snackChoice")?.let { sdk.chooseSnack(it) }
    }
    private fun guarded(action: () -> Unit) { if (safety.value.canInteract) action() }
    fun enterDemo() {
        if (safety.value.demoBuild && !safety.value.automotive)
            mutableSafety.update { it.copy(demoOptIn = true, sourceMessage = "정차 모의 시연 · 실제 주행 상태 아님") }
    }
    fun onForeground() {
        mutableSafety.update { it.copy(foreground = true, suspended = false) }
        ux.start(); gateway.start()
    }
    fun onBackground() {
        mutableSafety.update { it.copy(foreground = false) }
        if (!stopper.stop()) mutableSafety.update { it.copy(sourceMessage = "세션 정리 실패 · 안전 제한 유지") }
    }
    fun onDisplaySuspended() {
        mutableSafety.update { it.copy(suspended = true) }
        onBackground()
    }
    fun refreshVehicle() = guarded { gateway.refresh() }
    fun manual(scenario: Scenario) = guarded { sdk.trigger(scenario) }
    fun setNoise(value: Int) = guarded { sdk.setNoise(value) }
    fun setElapsed(value: Int) = guarded { sdk.setElapsed(value) }
    fun toggleMode() = guarded { sdk.toggleMode(); saved["cozyMode"] = state.value.cozyMode }
    fun chooseSnack(value: String) = guarded { sdk.chooseSnack(value); saved["snackChoice"] = value }
    fun answerRestStop(accepted: Boolean) = guarded { sdk.answerRestStop(accepted) }
    fun endConversation() = guarded { sdk.endConversation() }
    fun startTour() = guarded { sdk.startTour() }
    fun stopTour() = sdk.stopTour()
    fun reset() = guarded { sdk.reset(); saved.remove<Boolean>("cozyMode"); saved.remove<String>("snackChoice") }
    override fun onCleared() { stopper.stop(); super.onCleared() }
}
