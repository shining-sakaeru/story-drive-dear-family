package com.pleos.dearfamily.platform

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The demo flavor neither links Vehicle SDK nor asks for real vehicle permissions. */
object GatewayFactory {
    @Suppress("UNUSED_PARAMETER")
    fun create(context: Context, scope: CoroutineScope): VehicleReadGateway = object : VehicleReadGateway {
        override val demoOnly = true
        private val mutable = MutableStateFlow(VehicleReadState())
        override val state = mutable.asStateFlow()
        override fun start() = Unit
        override fun refresh() = Unit
        override fun stop() = Unit
    }
}
