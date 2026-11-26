package io.meridian.api

/**
 * Experimental M0.2 engine entry point.
 *
 * This prototype intentionally has no configured data sources. It validates the
 * consumer contract without pretending that production weather integrations exist.
 */
class MeridianEngine private constructor() {
    @Volatile
    private var closed: Boolean = false

    /**
     * Executes an explicit query. With no source adapters configured, the prototype
     * returns a typed unavailable result and performs no network or storage work.
     */
    suspend fun query(query: WeatherQuery): WeatherQueryResult {
        check(!closed) { "MeridianEngine is closed" }
        return WeatherQueryResult(
            status = QueryStatus.UNAVAILABLE,
            values = emptyList(),
            issues = query.variables.map { variable ->
                ComponentIssue(
                    variable = variable,
                    code = ComponentIssueCode.NO_DATA_SOURCE,
                    safeMessage = "No weather data source is configured in this prototype",
                )
            },
            unavailableReason = QueryUnavailableReason.NO_DATA_SOURCES_CONFIGURED,
        )
    }

    /** Releases this instance. Safe to call more than once. */
    fun close() {
        closed = true
    }

    companion object {
        /** Creates an engine with no persistence, network activity, or background work. */
        fun create(): MeridianEngine = MeridianEngine()
    }
}
