import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.zip.ZipFile

plugins { kotlin("jvm") version "1.9.25" }
repositories {
    google(); mavenCentral()
    maven { url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/") }
}
val selectedFlavor = providers.gradleProperty("flavor").orElse("demo").get()
require(selectedFlavor in listOf("demo", "pleos"))
val aarLibs by configurations.creating
val composeCompiler by configurations.creating
val aarCoordinates = listOf(
    "androidx.core:core:1.12.0", "androidx.core:core-ktx:1.12.0",
    "androidx.activity:activity:1.8.2", "androidx.activity:activity-ktx:1.8.2", "androidx.activity:activity-compose:1.8.2",
    "androidx.savedstate:savedstate:1.2.1", "androidx.savedstate:savedstate-ktx:1.2.1",
    "androidx.lifecycle:lifecycle-runtime:2.7.0", "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0",
    "androidx.lifecycle:lifecycle-runtime-compose:2.7.0", "androidx.lifecycle:lifecycle-viewmodel:2.7.0",
    "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0", "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0",
    "androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0",
    "androidx.compose.runtime:runtime-android:1.6.1", "androidx.compose.runtime:runtime-saveable-android:1.6.1",
    "androidx.compose.ui:ui-android:1.6.1", "androidx.compose.ui:ui-graphics-android:1.6.1",
    "androidx.compose.ui:ui-text-android:1.6.1", "androidx.compose.ui:ui-unit-android:1.6.1",
    "androidx.compose.ui:ui-geometry-android:1.6.1", "androidx.compose.ui:ui-util-android:1.6.1",
    "androidx.compose.foundation:foundation-android:1.6.1", "androidx.compose.foundation:foundation-layout-android:1.6.1",
    "androidx.compose.animation:animation-android:1.6.1", "androidx.compose.animation:animation-core-android:1.6.1",
    "androidx.compose.material3:material3-android:1.2.0", "androidx.compose.material:material-ripple-android:1.6.1",
    "androidx.compose.material:material-icons-core-android:1.6.1"
)
dependencies {
    implementation("org.robolectric:android-all:14-robolectric-10818077")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-common:2.7.0")
    implementation("androidx.annotation:annotation-jvm:1.7.1")
    implementation("androidx.collection:collection-jvm:1.4.0")
    composeCompiler("androidx.compose.compiler:compiler:1.5.15")
    if (selectedFlavor == "pleos") aarLibs("ai.pleos.playground:Vehicle:2.3.2@aar")
    aarCoordinates.forEach { aarLibs("$it@aar") }
    implementation(fileTree(layout.buildDirectory.dir("aar-jars/$selectedFlavor")) { include("*.jar") })
}
val extractAars by tasks.registering {
    inputs.files(aarLibs)
    outputs.dir(layout.buildDirectory.dir("aar-jars/$selectedFlavor"))
    doLast {
        val out = layout.buildDirectory.dir("aar-jars/$selectedFlavor").get().asFile.apply { mkdirs() }
        aarLibs.files.forEach { archive ->
            ZipFile(archive).use { zip ->
                zip.getEntry("classes.jar")?.let { entry ->
                    zip.getInputStream(entry).use { input -> out.resolve(archive.nameWithoutExtension + ".jar").outputStream().use { input.copyTo(it) } }
                }
            }
        }
    }
}
kotlin { jvmToolchain(17) }
sourceSets.main { java.setSrcDirs(listOf("../../app/src/main/java", "../../app/src/$selectedFlavor/java")) }
kotlin.sourceSets.main { kotlin.setSrcDirs(listOf("../../app/src/main/java", "../../app/src/$selectedFlavor/java")) }
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(extractAars)
    kotlinOptions { jvmTarget = "17"; freeCompilerArgs += listOf("-Xplugin=${composeCompiler.singleFile.absolutePath}") }
}
