plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":engine"))
}

application {
    mainClass.set("io.meridian.consumer.MainKt")
}
