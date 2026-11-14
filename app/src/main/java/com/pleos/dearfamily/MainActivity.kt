package com.pleos.dearfamily

import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.pleos.dearfamily.ui.DearFamilyMainScreen
import com.pleos.dearfamily.platform.AudioNarrator
import com.pleos.dearfamily.ui.DearFamilyViewModel
import com.pleos.dearfamily.ui.theme.DearFamilyTheme

class MainActivity : ComponentActivity() {
    private lateinit var narrator: AudioNarrator
    internal val narrationStatus get() = narrator.status
    internal val narrationFeedback get() = narrator.feedback
    private val model: DearFamilyViewModel by viewModels()
    private val displayOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                narrator.setAllowed(false)
                model.onDisplaySuspended()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Stay inside the platform-provided app window; do not hide the OS window bar.
        narrator = AudioNarrator(applicationContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                narrator.feedback.collect { model.onNarrationFeedback(it) }
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(this, displayOff,
            IntentFilter(Intent.ACTION_SCREEN_OFF), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        setContent { DearFamilyTheme { DearFamilyMainScreen(narrator = narrator, vm = model) } }
    }
    override fun onResume() { super.onResume(); model.onForeground() }
    override fun onStop() { narrator.setAllowed(false); model.onBackground(); super.onStop() }
    override fun onDestroy() { unregisterReceiver(displayOff); narrator.release(); super.onDestroy() }
}
