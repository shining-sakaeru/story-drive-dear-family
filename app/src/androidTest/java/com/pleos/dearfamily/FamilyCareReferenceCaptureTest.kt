package com.pleos.dearfamily

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Captures real MainActivity theme roots and theme content for parent-side review. */
class FamilyCareReferenceCaptureTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureThreeThemeReferencesAndMetadata() {
        val hasEnterDemo = compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()
        if (hasEnterDemo) compose.onNodeWithTag("enterDemo").performClick()
        val pickerAvailable = runCatching {
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("themePicker").fetchSemanticsNodes().isNotEmpty()
            }
            true
        }.getOrDefault(false)
        assumeTrue("MainActivity theme entry is unavailable; capture skipped", pickerAvailable)

        val outputDir = File(compose.activity.filesDir, "reference-captures").apply { mkdirs() }
        val captures = JSONArray()
        val themes = listOf(
            "GENESIS_LUXURY" to "genesisContent",
            "SCANDINAVIAN_WARM" to "warmContent",
            "SPATIAL_WIDGET" to "spatialContent"
        )

        themes.forEach { (theme, contentTag) ->
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("themePicker").performClick()
            compose.onNodeWithTag("theme_$theme").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag(contentTag).fetchSemanticsNodes().isNotEmpty()
            }
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithTag(contentTag).assertExists()

            val root = compose.onRoot()
            val content = compose.onNodeWithTag(contentTag)
            val rootBounds = root.fetchSemanticsNode().boundsInRoot
            val contentBounds = content.fetchSemanticsNode().boundsInRoot
            val rootName = "${theme.lowercase(Locale.US)}-root.png"
            val contentName = "${theme.lowercase(Locale.US)}-content.png"
            writePng(root.captureToImage().asAndroidBitmap(), File(outputDir, rootName))
            writePng(content.captureToImage().asAndroidBitmap(), File(outputDir, contentName))

            captures.put(JSONObject()
                .put("theme", theme)
                .put("rootFile", rootName)
                .put("themeFile", contentName)
                .put("rootBounds", rectJson(rootBounds.left, rootBounds.top, rootBounds.right, rootBounds.bottom))
                .put("themeBounds", rectJson(contentBounds.left, contentBounds.top, contentBounds.right, contentBounds.bottom)))
        }

        val metrics = compose.activity.resources.displayMetrics
        val configuration = compose.activity.resources.configuration
        val appView = compose.activity.window.decorView
        val appBounds = rectJson(0f, 0f, appView.width.toFloat(), appView.height.toFloat())
        val locales = JSONArray()
        for (index in 0 until configuration.locales.size()) {
            locales.put(configuration.locales[index].toLanguageTag())
        }
        val revision = InstrumentationRegistry.getArguments().getString("sourceRevision") ?: "unrecorded"
        val metadata = JSONObject()
            .put("sourceRevision", revision)
            .put("appBounds", appBounds)
            .put("density", metrics.density)
            .put("fontScale", configuration.fontScale)
            .put("locale", locales)
            .put("captures", captures)
        File(outputDir, "metadata.json").writeText(metadata.toString(2))
    }

    private fun rectJson(left: Float, top: Float, right: Float, bottom: Float): JSONObject =
        JSONObject()
            .put("left", left)
            .put("top", top)
            .put("right", right)
            .put("bottom", bottom)
            .put("width", right - left)
            .put("height", bottom - top)

    private fun writePng(bitmap: Bitmap, file: File) {
        file.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write capture ${file.name}"
            }
        }
    }
}
