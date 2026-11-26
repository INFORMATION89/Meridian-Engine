package io.meridian.contractspike

/**
 * Marks the M0.2 contract prototype as deliberately unstable.
 *
 * This opt-in marker is temporary milestone evidence, not a promise that these
 * names or semantics will ship in the stable public API.
 */
@RequiresOptIn(
    message = "Experimental M0.2 contract spike. Do not depend on this API in production.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
annotation class ExperimentalWeatherContractSpike
