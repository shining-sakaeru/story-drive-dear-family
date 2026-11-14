plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.pleos.dearfamily"
    compileSdk = 34
    defaultConfig { applicationId = "com.pleos.dearfamily"; minSdk = 29; targetSdk = 34; versionCode = 1; versionName = "1.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { compose = true; buildConfig = false }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
    flavorDimensions += "runtime"
    productFlavors {
        create("demo") { dimension = "runtime"; applicationIdSuffix = ".demo"; versionNameSuffix = "-demo" }
        create("pleos") { dimension = "runtime" }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    "pleosImplementation"("ai.pleos.playground:Vehicle:2.3.2")
    "pleosImplementation"("ai.pleos.playground:TextToSpeech:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
