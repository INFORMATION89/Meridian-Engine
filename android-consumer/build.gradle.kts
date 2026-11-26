plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.meridian.androidconsumer"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.meridian.androidconsumer"
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":engine"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
