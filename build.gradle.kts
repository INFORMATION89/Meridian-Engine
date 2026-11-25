plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Lock every resolvable configuration once the lock state has been generated
// and committed. This keeps platform-specific dependency graphs in scope.
allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}
