plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.meridian.smoke"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.meridian.smoke"
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":probe"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.sqldelight.android.driver)
    androidTestImplementation(libs.ktor.client.android)
    androidTestImplementation(libs.ktor.client.cio)
}
