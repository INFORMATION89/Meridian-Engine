plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":probe"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sqldelight.sqlite.driver)
}

application {
    mainClass = "org.meridian.desktop.MainKt"
}
