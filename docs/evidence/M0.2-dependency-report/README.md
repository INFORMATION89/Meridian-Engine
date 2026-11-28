# M0.2 Gradle dependency report (raw evidence)

Task H2 in `AI_IMPLEMENTATION_PLAN.md` section 13. This folder holds raw Gradle output only. It has not been analysed; analysis belongs to task S5.

## Run details

- Commit SHA the commands ran on: `86bc39e1593a73bfc535070e4b5584e94d57ba71` (`origin/main` at the time of the run)
- Date (UTC): 2026-11-28
- Gradle wrapper: 9.7.0 (`./gradlew --version`)
- Kotlin (Gradle plugin runtime): 2.4.0
- Groovy: 4.0.32
- Launcher JVM: 21.0.13.1 (Ubuntu 21.0.13.1+2-1-24.04.1-Ubuntu), amd64
- OS: Linux 6.18.44-fc-v880 amd64
- Configuration inspected for `dependencyInsight`: `desktopRuntimeClasspath` (runtime classpath of the `desktop` JVM target, the main runtime configuration of the desktop target)

## Commands

Run from the repository root, in this order:

```bash
./gradlew --version --console=plain
./gradlew :engine:dependencies --console=plain > docs/evidence/M0.2-dependency-report/engine-dependencies.txt
./gradlew :engine:dependencyInsight --dependency org.jetbrains.kotlin:kotlin-stdlib --configuration desktopRuntimeClasspath --console=plain > docs/evidence/M0.2-dependency-report/insight-org-jetbrains-kotlin_kotlin-stdlib-desktopRuntimeClasspath.txt
./gradlew :engine:dependencyInsight --dependency io.ktor:ktor-client-core --configuration desktopRuntimeClasspath --console=plain > docs/evidence/M0.2-dependency-report/insight-io-ktor_ktor-client-core-desktopRuntimeClasspath.txt
./gradlew :engine:dependencyInsight --dependency app.cash.sqldelight --configuration desktopRuntimeClasspath --console=plain > docs/evidence/M0.2-dependency-report/insight-app-cash-sqldelight-desktopRuntimeClasspath.txt
./gradlew :engine:dependencyInsight --dependency org.jetbrains.kotlinx:kotlinx-coroutines-core --configuration desktopRuntimeClasspath --console=plain > docs/evidence/M0.2-dependency-report/insight-org-jetbrains-kotlinx_kotlinx-coroutines-core-desktopRuntimeClasspath.txt
```

Stderr from the commands was kept out of these files. The `JAVA_TOOL_OPTIONS` and `_JAVA_OPTIONS` notices go to stderr.

## Files

| File | Source command |
|---|---|
| `engine-dependencies.txt` | `:engine:dependencies` (all configurations) |
| `insight-org-jetbrains-kotlin_kotlin-stdlib-desktopRuntimeClasspath.txt` | `dependencyInsight` for `org.jetbrains.kotlin:kotlin-stdlib` |
| `insight-io-ktor_ktor-client-core-desktopRuntimeClasspath.txt` | `dependencyInsight` for `io.ktor:ktor-client-core` |
| `insight-app-cash-sqldelight-desktopRuntimeClasspath.txt` | `dependencyInsight` for `app.cash.sqldelight` (group-level match) |
| `insight-org-jetbrains-kotlinx_kotlinx-coroutines-core-desktopRuntimeClasspath.txt` | `dependencyInsight` for `org.jetbrains.kotlinx:kotlinx-coroutines-core` |

## Repository state

No build file, `gradle.lockfile`, or verification metadata was changed. Gradle did not ask to write any of them. `git status` after the runs showed only this folder as new.
