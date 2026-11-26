package io.meridian.api

import kotlin.concurrent.Volatile

/**
 * Experimental M0.2 engine entry point.
 *
 * The public factory currently has no configured data sources. The internal executor
 * seam exists only for deterministic contract tests and is not part of the consumer API.
 */
class MeridianEngine private constructor(
    private val queryExecutor: suspend (WeatherQuery) -> WeatherQueryResult,
) {
    @Volatile
    private var closed: Boolean = false

    /**
     * Executes an explicit query. The default prototype returns a typed unavailable
     * result and performs no network or storage work.
     */
    @Throws(MeridianEngineClosedException::class)
    suspend fun query(query: WeatherQuery): WeatherQueryResult {
        if (closed) throw MeridianEngineClosedException()
        return queryExecutor(query)
    }

    /** Releases this instance. Safe to call more than once. */
    fun close() {
        closed = true
    }

    companion object {
        /** Creates an engine with no persistence, network activity, or background work. */
        fun create(): MeridianEngine = MeridianEngine { query ->
            WeatherQueryResult(
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

        /** Test-only seam; deliberately internal and absent from the consumer API. */
        internal fun createForTesting(
            queryExecutor: suspend (WeatherQuery) -> WeatherQueryResult,
        ): MeridianEngine = MeridianEngine(queryExecutor)
    }
}

/** Raised when a query is submitted after its engine has been closed. */
class MeridianEngineClosedException : IllegalStateException("MeridianEngine is closed")
