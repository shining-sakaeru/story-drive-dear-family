plugins { kotlin("jvm") version "1.9.25" }

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

sourceSets {
    main { kotlin.srcDirs("../app/src/main/java/com/pleos/dearfamily/model", "../app/src/main/java/com/pleos/dearfamily/domain", "../app/src/main/java/com/pleos/dearfamily/sdk"); kotlin.exclude("**/OfflineKoreanTts.kt") }
    test { kotlin.srcDir("src/test/kotlin") }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
}
tasks.test { useJUnitPlatform() }
