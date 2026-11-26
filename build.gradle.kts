plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Lock every resolvable configuration once it is used by a real build/test task.
// The CI metadata-generation job runs each platform's dependency-consuming tasks
// with --write-locks so Gradle captures the configurations it actually resolves.
allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}
