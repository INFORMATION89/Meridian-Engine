package io.meridian.contractspike

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * M0.2 design spike only. These types are deliberately isolated from the stable
 * probe package and are not an approved or versioned public API.
 */
@ExperimentalWeatherContractSpike\nenum class WeatherFeature {
    CURRENT_CONDITIONS,
    HOURLY_FORECAST,
}

@ExperimentalWeatherContractSpike\nenum class WeatherVariable {
    AIR_TEMPERATURE,
    APPARENT_TEMPERATURE,
}

@ExperimentalWeatherContractSpike\nenum class WeatherUnit {
    CELSIUS,
}

@ExperimentalWeatherContractSpike\nenum class DataFreshness {
    FRESH,
    STALE,
}

@ExperimentalWeatherContractSpike\nenum class CachePolicy {
    NETWORK_ONLY,
    PREFER_FRESH,
    ALLOW_STALE,
}

@ExperimentalWeatherContractSpike\nenum class WeatherProblemCode {
    SOURCE_UNAVAILABLE,
    SOURCE_FAILURE,
    TRANSPORT_FAILURE,
    RATE_LIMITED,
    STORAGE_READ_FAILED,
    STORAGE_WRITE_FAILED,
    STALE_CACHE_USED,
}

@ExperimentalWeatherContractSpike\ndata class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0) {
            "Latitude must be finite and between -90 and 90 degrees."
        }
        require(longitude.isFinite() && longitude in -180.0..180.0) {
            "Longitude must be finite and between -180 and 180 degrees."
        }
    }
}

@ExperimentalWeatherContractSpike\ndata class WeatherRequest(
    val location: GeoCoordinates,
    val features: Set<WeatherFeature>,
    val cachePolicy: CachePolicy = CachePolicy.NETWORK_ONLY,
    val maxStaleAgeMillis: Long = 0,
) {
    init {
        require(features.isNotEmpty()) { "At least one weather feature must be requested." }
        require(maxStaleAgeMillis >= 0) { "Maximum stale age cannot be negative." }
        require(cachePolicy == CachePolicy.ALLOW_STALE || maxStaleAgeMillis == 0L) {
            "A stale-age limit is only valid with ALLOW_STALE."
        }
    }
}

@ExperimentalWeatherContractSpike\ndata class WeatherValue(
    val variable: WeatherVariable,
    val value: Double,
    val unit: WeatherUnit,
) {
    init {
        require(value.isFinite()) { "Weather values must be finite." }
    }
}

@ExperimentalWeatherContractSpike\ndata class SourceProvenance(
    val sourceId: String,
    val sourceVersion: String?,
)

@ExperimentalWeatherContractSpike\ndata class WeatherProblem(
    val code: WeatherProblemCode,
    val detail: String? = null,
)

@ExperimentalWeatherContractSpike\ndata class WeatherFeatureData(
    val feature: WeatherFeature,
    val values: List<WeatherValue>,
    val provenance: SourceProvenance,
    val issuedAtEpochMillis: Long?,
    val observedAtEpochMillis: Long?,
    val fetchedAtEpochMillis: Long,
    val validAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val freshness: DataFreshness = DataFreshness.FRESH,
) {
    /**
     * Forecast lead is the interval from issuance to the valid instant.
     * Observations have no forecast lead and retain their measurement instant separately.
     */
    val forecastLeadMillis: Long?
        get() = issuedAtEpochMillis?.let { validAtEpochMillis - it }

    init {
        require(values.isNotEmpty()) { "Feature data must contain at least one value." }
        require(issuedAtEpochMillis == null || issuedAtEpochMillis <= fetchedAtEpochMillis) {
            "Forecast issue time cannot be later than fetch time."
        }
        require(observedAtEpochMillis == null || observedAtEpochMillis <= fetchedAtEpochMillis) {
            "Observation time cannot be later than fetch time."
        }
        require(issuedAtEpochMillis == null || validAtEpochMillis >= issuedAtEpochMillis) {
            "Forecast valid time cannot be earlier than issue time."
        }
        require(expiresAtEpochMillis > fetchedAtEpochMillis) {
            "Cache expiry must be later than fetch time."
        }
    }
}

@ExperimentalWeatherContractSpike\nsealed interface FeatureOutcome {
    data class Available(val data: WeatherFeatureData) : FeatureOutcome
    data class Partial(
        val data: WeatherFeatureData,
        val problems: List<WeatherProblem>,
    ) : FeatureOutcome
    data class Unavailable(val problem: WeatherProblem) : FeatureOutcome
    data class Failed(val problem: WeatherProblem) : FeatureOutcome
}

@ExperimentalWeatherContractSpike\ndata class WeatherResult(
    val outcomes: Map<WeatherFeature, FeatureOutcome>,
    val completedAtEpochMillis: Long,
)

@ExperimentalWeatherContractSpike\ndata class FeatureCacheKey(
    val latitude: Double,
    val longitude: Double,
    val feature: WeatherFeature,
)

@ExperimentalWeatherContractSpike\ninterface WeatherClock {
    fun nowEpochMillis(): Long
}

@ExperimentalWeatherContractSpike\ninterface WeatherSource {
    suspend fun fetchFeature(
        request: WeatherRequest,
        feature: WeatherFeature,
    ): FeatureOutcome
}

/**
 * A storage port for the spike. A null store means no persistence at all.
 * The first implementation is intentionally in-memory and test-oriented.
 */
@ExperimentalWeatherContractSpike\ninterface WeatherStore {
    suspend fun read(key: FeatureCacheKey): WeatherFeatureData?
    suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData)
    suspend fun clear()
}

@ExperimentalWeatherContractSpike\nclass InMemoryWeatherStore : WeatherStore {
    private val mutex = Mutex()
    private val entries = mutableMapOf<FeatureCacheKey, WeatherFeatureData>()

    override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? =
        mutex.withLock { entries[key] }

    override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) {
        mutex.withLock { entries[key] = data }
    }

    override suspend fun clear() {
        mutex.withLock { entries.clear() }
    }
}

/**
 * Experimental request orchestrator. It has no provider, network, database,
 * background scheduler, or consumer-application dependency.
 */
@ExperimentalWeatherContractSpike\nclass WeatherContractEngine(
    private val source: WeatherSource,
    private val clock: WeatherClock,
    private val store: WeatherStore? = null,
) {
    suspend fun fetch(request: WeatherRequest): WeatherResult {
        val outcomes = linkedMapOf<WeatherFeature, FeatureOutcome>()
        for (feature in request.features.sortedBy { it.name }) {
            outcomes[feature] = fetchFeature(request, feature)
        }
        return WeatherResult(outcomes, clock.nowEpochMillis())
    }

    private suspend fun fetchFeature(
        request: WeatherRequest,
        feature: WeatherFeature,
    ): FeatureOutcome {
        val key = FeatureCacheKey(
            request.location.latitude,
            request.location.longitude,
            feature,
        )
        var cached: WeatherFeatureData? = null
        var cacheReadProblem: WeatherProblem? = null

        if (request.cachePolicy != CachePolicy.NETWORK_ONLY && store != null) {
            try {
                cached = store.read(key)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                cacheReadProblem = WeatherProblem(
                    WeatherProblemCode.STORAGE_READ_FAILED,
                    failure.message,
                )
            }
            val now = clock.nowEpochMillis()
            val fresh = cached?.takeIf {
                now >= it.fetchedAtEpochMillis && now < it.expiresAtEpochMillis
            }
            if (fresh != null) {
                return FeatureOutcome.Available(fresh.copy(freshness = DataFreshness.FRESH))
            }
        }

        val sourceOutcome = try {
            source.fetchFeature(request, feature)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            FeatureOutcome.Failed(
                WeatherProblem(WeatherProblemCode.TRANSPORT_FAILURE, failure.message),
            )
        }

        val data = when (sourceOutcome) {
            is FeatureOutcome.Available -> sourceOutcome.data
            is FeatureOutcome.Partial -> sourceOutcome.data
            is FeatureOutcome.Unavailable,
            is FeatureOutcome.Failed -> null
        }

        if (data != null && store != null) {
            try {
                store.write(key, data)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                val problem = WeatherProblem(
                    WeatherProblemCode.STORAGE_WRITE_FAILED,
                    failure.message,
                )
                return FeatureOutcome.Partial(
                    data,
                    (sourceOutcome as? FeatureOutcome.Partial)?.problems.orEmpty() + problem,
                )
            }
        }

        if (cacheReadProblem != null && data != null) {
            return FeatureOutcome.Partial(
                data,
                (sourceOutcome as? FeatureOutcome.Partial)?.problems.orEmpty() + listOfNotNull(cacheReadProblem),
            )
        }

        if (request.cachePolicy == CachePolicy.ALLOW_STALE && cached != null) {
            val now = clock.nowEpochMillis()
            val staleAge = now - cached.fetchedAtEpochMillis
            if (staleAge >= 0 && staleAge <= request.maxStaleAgeMillis &&
                sourceOutcome !is FeatureOutcome.Available &&
                sourceOutcome !is FeatureOutcome.Partial
            ) {
                return FeatureOutcome.Partial(
                    cached.copy(freshness = DataFreshness.STALE),
                    listOf(
                        (sourceOutcome as? FeatureOutcome.Unavailable)?.problem
                            ?: (sourceOutcome as? FeatureOutcome.Failed)?.problem
                            ?: WeatherProblem(WeatherProblemCode.SOURCE_UNAVAILABLE),
                        WeatherProblem(WeatherProblemCode.STALE_CACHE_USED),
                    ),
                )
            }
        }
        return sourceOutcome
    }
}

/**
 * A deterministic fixture used only by the standalone Kotlin, Android and Swift
 * consumers in this design spike. It is not a weather provider.
 */
@ExperimentalWeatherContractSpike\nobject WeatherContractDemo {
    private class FixedClock(private val now: Long) : WeatherClock {
        override fun nowEpochMillis(): Long = now
    }

    private class FixtureSource : WeatherSource {
        override suspend fun fetchFeature(
            request: WeatherRequest,
            feature: WeatherFeature,
        ): FeatureOutcome = when (feature) {
            WeatherFeature.CURRENT_CONDITIONS -> FeatureOutcome.Available(
                WeatherFeatureData(
                    feature = feature,
                    values = listOf(
                        WeatherValue(WeatherVariable.AIR_TEMPERATURE, 18.0, WeatherUnit.CELSIUS),
                        WeatherValue(WeatherVariable.APPARENT_TEMPERATURE, 21.0, WeatherUnit.CELSIUS),
                    ),
                    provenance = SourceProvenance("fixture-local", "m0.2-spike"),
                    issuedAtEpochMillis = null,
                    observedAtEpochMillis = 950,
                    fetchedAtEpochMillis = 1_000,
                    validAtEpochMillis = 950,
                    expiresAtEpochMillis = 2_000,
                ),
            )
            WeatherFeature.HOURLY_FORECAST -> FeatureOutcome.Unavailable(
                WeatherProblem(WeatherProblemCode.SOURCE_UNAVAILABLE, "No fixture for this feature."),
            )
        }
    }

    fun createEngine(store: WeatherStore? = null): WeatherContractEngine =
        WeatherContractEngine(FixtureSource(), FixedClock(1_500), store)

    fun sampleRequest(
        cachePolicy: CachePolicy = CachePolicy.NETWORK_ONLY,
        maxStaleAgeMillis: Long = 0,
    ): WeatherRequest = WeatherRequest(
        location = GeoCoordinates(19.4326, -99.1332),
        features = setOf(WeatherFeature.CURRENT_CONDITIONS, WeatherFeature.HOURLY_FORECAST),
        cachePolicy = cachePolicy,
        maxStaleAgeMillis = maxStaleAgeMillis,
    )

    suspend fun verifySampleRequest(): String {
        val result = createEngine().fetch(sampleRequest())
        val current = result.outcomes[WeatherFeature.CURRENT_CONDITIONS]
        val hourly = result.outcomes[WeatherFeature.HOURLY_FORECAST]
        check(current is FeatureOutcome.Available)
        check(hourly is FeatureOutcome.Unavailable)
        check(current.data.values.any { it.variable == WeatherVariable.AIR_TEMPERATURE && it.value == 18.0 })
        check(current.data.values.any { it.variable == WeatherVariable.APPARENT_TEMPERATURE && it.value == 21.0 })
        check(current.data.observedAtEpochMillis == 950L)
        check(current.data.issuedAtEpochMillis == null)
        check(current.data.forecastLeadMillis == null)
        return "current=available;hourly=unavailable;air=18.0C;apparent=21.0C"
    }
}
