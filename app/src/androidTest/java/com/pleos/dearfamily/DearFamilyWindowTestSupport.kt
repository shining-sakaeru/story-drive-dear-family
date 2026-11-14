package com.pleos.dearfamily

import android.os.SystemClock
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.AndroidComposeTestRule


/** AAOS may reparent a newly launched task after its first RESUMED callback. */
fun awaitDearFamilyWindow(compose: AndroidComposeTestRule<*, MainActivity>) {
    var visibleSince = 0L
    compose.waitUntil(10_000) {
        val visible = runCatching {
            compose.runOnUiThread {
                compose.activity.window.decorView.isShown && compose.activity.hasWindowFocus()
            } && compose.onAllNodes(isRoot()).fetchSemanticsNodes().isNotEmpty()
        }.getOrDefault(false)
        val now = SystemClock.elapsedRealtime()
        if (!visible) {
            visibleSince = 0L
            false
        } else {
            if (visibleSince == 0L) visibleSince = now
            now - visibleSince >= 1_000L
        }
    }
}
