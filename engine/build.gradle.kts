import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "io.meridian.engine"
        compileSdk = 36
        minSdk = 26
    }

    val xcf = XCFramework("MeridianShared")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "MeridianShared"
            isStatic = true
            // SQLDelight's Native driver uses Apple's system SQLite library.
            linkerOpts("-lsqlite3")
            xcf.add(this)
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.sqldelight.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        val desktopMain by getting {
            dependencies {
            implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        val desktopTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
    }
}

sqldelight {
    databases {
        register("ProbeDatabase") {
            packageName.set("io.meridian.probe.db")
        }
    }
}
