plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.sqldelight) apply false
}

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}

tasks.register("printM01ArtifactSizes") {
    mustRunAfter(
        ":probe:jvmJar",
        ":desktop-smoke:installDist",
        ":android-smoke:assembleDebug",
        ":probe:linkDebugFrameworkIosArm64",
        ":probe:linkDebugFrameworkIosSimulatorArm64",
    )
    doLast {
        val paths = listOf(
            "probe/build/libs/probe-jvm.jar",
            "desktop-smoke/build/install/desktop-smoke/lib",
            "android-smoke/build/outputs/apk/debug/android-smoke-debug.apk",
            "probe/build/bin/iosArm64/debugFramework/MeridianProbe.framework",
            "probe/build/bin/iosSimulatorArm64/debugFramework/MeridianProbe.framework",
        )
        paths.forEach { relative ->
            val artifact = layout.projectDirectory.file(relative).asFile
            if (artifact.isFile) {
                println("M0.1 artifact ${artifact.path}: ${artifact.length()} bytes")
            } else if (artifact.isDirectory) {
                val size = artifact.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                println("M0.1 artifact ${artifact.path}: $size bytes total across its files")
            } else {
                println("M0.1 artifact ${artifact.path}: not produced on this job")
            }
        }
    }
}
