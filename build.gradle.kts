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

// Resolve every project's resolvable configurations in one controlled pass.
// Run with --write-locks to generate the committed Gradle lock state.
tasks.register("resolveAndLockAll") {
    notCompatibleWithConfigurationCache("Resolves project configurations at execution time")

    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Run this task with --write-locks to generate dependency lock state."
        }
    }

    doLast {
        allprojects.forEach { project ->
            project.configurations
                .filter { it.isCanBeResolved }
                .forEach { configuration ->
                    project.logger.lifecycle("Resolving ${project.path}:${configuration.name}")
                    configuration.resolve()
                }
        }
    }
}
