plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Lock every resolvable configuration so platform CI consumes the committed lockfiles.
// Dependency locks and artifact checksums are refreshed only through an explicit
// maintenance change; pull-request CI never mutates the branch it is validating.
allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}
