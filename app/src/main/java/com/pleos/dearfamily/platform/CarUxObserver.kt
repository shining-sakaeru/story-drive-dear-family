package com.pleos.dearfamily.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import java.lang.reflect.Proxy

/** Optional shared-library boundary. Public Car APIs are reflected so demo needs no car.jar. */
class CarUxObserver(context: Context,
    private val changed: (known: Boolean, requiresOptimization: Boolean, flags: Int, message: String) -> Unit) {
    private val app = context.applicationContext
    val automotive = app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    private val main = Handler(Looper.getMainLooper())
    private var car: Any? = null
    private var manager: Any? = null
    private var generation = 0L
    private var running = false

    fun start() {
        if (running) return
        running = true
        val epoch = ++generation
        if (!automotive) {
            changed(false, true, 0, "일반 Android · 실제 차량 규제 상태 없음")
            return
        }
        changed(false, true, 0, "CarUxRestrictions 연결 중 · 안전 제한")
        try {
            val carType = Class.forName("android.car.Car")
            val lifecycleType = Class.forName("android.car.Car\$CarServiceLifecycleListener")
            val listener = Proxy.newProxyInstance(lifecycleType.classLoader, arrayOf(lifecycleType)) { proxy, method, args ->
                when (method.name) {
                    "onLifecycleChanged" -> {
                        val instance = args?.getOrNull(0)
                        val ready = args?.getOrNull(1) as? Boolean ?: false
                        main.post {
                            if (epoch == generation && running) {
                                if (ready && instance != null) bindManager(carType, instance, epoch)
                                else changed(false, true, 0, "Car 서비스 연결 끊김 · 안전 제한")
                            }
                        }
                        null
                    }
                    "toString" -> "DearFamilyCarLifecycleListener"
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> proxy === args?.getOrNull(0)
                    else -> null
                }
            }
            val wait = carType.getField("CAR_WAIT_TIMEOUT_DO_NOT_WAIT").getLong(null)
            car = carType.getMethod("createCar", Context::class.java, Handler::class.java,
                Long::class.javaPrimitiveType, lifecycleType).invoke(null, app, main, wait, listener)
            if (car == null) changed(false, true, 0, "Car 서비스 없음 · 안전 제한")
        } catch (_: Exception) { changed(false, true, 0, "Car UX API/권한 사용 불가 · 안전 제한") }
        catch (_: LinkageError) { changed(false, true, 0, "Car 공유 라이브러리 없음 · 안전 제한") }
    }
    private fun bindManager(carType: Class<*>, instance: Any, epoch: Long) {
        try {
            val service = carType.getField("CAR_UX_RESTRICTION_SERVICE").get(null) as String
            val ux = carType.getMethod("getCarManager", String::class.java).invoke(instance, service)
                ?: throw IllegalStateException("No UX manager")
            manager = ux
            val listenerType = Class.forName("android.car.drivingstate.CarUxRestrictionsManager\$OnUxRestrictionsChangedListener")
            val listener = Proxy.newProxyInstance(listenerType.classLoader, arrayOf(listenerType)) { proxy, method, args ->
                when (method.name) {
                    "onUxRestrictionsChanged" -> {
                        val restrictions = args?.getOrNull(0)
                        main.post { if (running && epoch == generation && restrictions != null) accept(restrictions) }
                        null
                    }
                    "toString" -> "DearFamilyUxListener"
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> proxy === args?.getOrNull(0)
                    else -> null
                }
            }
            ux.javaClass.getMethod("registerListener", listenerType).invoke(ux, listener)
            val current = ux.javaClass.getMethod("getCurrentCarUxRestrictions").invoke(ux)
            if (current != null) accept(current)
        } catch (_: Exception) { changed(false, true, 0, "차량 UX 제한 조회 실패 · 안전 제한") }
    }
    private fun accept(restrictions: Any) {
        try {
            val needs = restrictions.javaClass.getMethod("isRequiresDistractionOptimization").invoke(restrictions) as Boolean
            val flags = restrictions.javaClass.getMethod("getActiveRestrictions").invoke(restrictions) as Int
            changed(true, needs, flags, if (needs || flags != 0) "플랫폼 주행 UX 제한 활성" else "플랫폼 정차 UX 확인")
        } catch (_: Exception) { changed(false, true, 0, "UX 상태 해석 실패 · 안전 제한") }
    }
    fun stop() {
        running = false
        generation++
        runCatching { manager?.javaClass?.getMethod("unregisterListener")?.invoke(manager) }
        runCatching { car?.javaClass?.getMethod("disconnect")?.invoke(car) }
        manager = null; car = null
        changed(false, true, 0, "Car UX 구독 해제 · 안전 제한")
    }
}
