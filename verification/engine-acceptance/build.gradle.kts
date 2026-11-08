plugins { kotlin("jvm") version "1.9.25" }
repositories { mavenCentral() }
kotlin { jvmToolchain(17) }
kotlin.sourceSets.main {
    kotlin.setSrcDirs(listOf("../../app/src/main/java"))
    kotlin.include("com/pleos/dearfamily/model/**", "com/pleos/dearfamily/domain/**", "com/pleos/dearfamily/sdk/**", "com/pleos/dearfamily/policy/**")
}
kotlin.sourceSets.test {
    kotlin.srcDir("../../engine-tests/src/test/kotlin/com/pleos/dearfamily/sdk")
    kotlin.srcDir("../../app/src/test/java")
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
tasks.test { testLogging { events("passed", "failed", "skipped"); showStandardStreams = true } }
