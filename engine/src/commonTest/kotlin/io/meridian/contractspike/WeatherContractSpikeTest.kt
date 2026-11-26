package io.meridian.contractspike

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WeatherContractSpikeTest {
    @Test
    fun noStorageModeReturnsIndependentFeatureOutcomes() = runBlocking {
        assertEquals(
            "current=available;hourly=unavailable;air=18.0C;apparent=21.0C",
            WeatherContractDemo.verifySampleRequest(),
        )
    }

    @Test
    fun freshCachedFeatureDoesNotCallSource() = runBlocking {
        val store = InMemoryWeatherStore()
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
        val key = FeatureCacheKey(
            request.location.latitude,
            request.location.longitude,
            WeatherFeature.CURRENT_CONDITIONS,
        )
        val cached = WeatherFeatureData(
            feature = WeatherFeature.CURRENT_CONDITIONS,
            values = listOf(WeatherValue(WeatherVariable.AIR_TEMPERATURE, 18.0, WeatherUnit.CELSIUS)),
            provenance = SourceProvenance("fixture-local", "cached"),
            issuedAtEpochMillis = 900,
            fetchedAtEpochMillis = 1_000,
            validAtEpochMillis = 1_400,
            expiresAtEpochMillis = 2_000,
        )
        store.write(key, cached)

        var sourceCalls = 0
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(request: WeatherRequest, feature: WeatherFeature): FeatureOutcome {
                    sourceCalls += 1
                    return FeatureOutcome.Unavailable(WeatherProblem(WeatherProblemCode.SOURCE_UNAVAILABLE))
                }
            },
            clock = object : WeatherClock {
                override fun nowEpochMillis(): Long = 1_500
            },
            store = store,
        )
        val result = engine.fetch(request.copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS)))
        assertIs<FeatureOutcome.Available>(result.outcomes[WeatherFeature.CURRENT_CONDITIONS])
        assertEquals(0, sourceCalls)
    }

    @Test
    fun staleDataIsExplicitAndOnlyUsedWhenAllowed() = runBlocking {
        val store = InMemoryWeatherStore()
        val request = WeatherContractDemo.sampleRequest(
            cachePolicy = CachePolicy.ALLOW_STALE,
            maxStaleAgeMillis = 10_000,
        )
        val key = FeatureCacheKey(
            request.location.latitude,
            request.location.longitude,
            WeatherFeature.CURRENT_CONDITIONS,
        )
        store.write(
            key,
            WeatherFeatureData(
                feature = WeatherFeature.CURRENT_CONDITIONS,
                values = listOf(WeatherValue(WeatherVariable.AIR_TEMPERATURE, 18.0, WeatherUnit.CELSIUS)),
                provenance = SourceProvenance("fixture-local", "cached"),
                issuedAtEpochMillis = 900,
                fetchedAtEpochMillis = 500,
                validAtEpochMillis = 800,
                expiresAtEpochMillis = 1_000,
            ),
        )
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(request: WeatherRequest, feature: WeatherFeature) =
                    FeatureOutcome.Unavailable(WeatherProblem(WeatherProblemCode.SOURCE_UNAVAILABLE))
            },
            clock = object : WeatherClock {
                override fun nowEpochMillis(): Long = 1_500
            },
            store = store,
        )
        val result = engine.fetch(request.copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS)))
        val partial = assertIs<FeatureOutcome.Partial>(result.outcomes[WeatherFeature.CURRENT_CONDITIONS])
        assertEquals(DataFreshness.STALE, partial.data.freshness)
        assertTrue(partial.problems.any { it.code == WeatherProblemCode.STALE_CACHE_USED })
    }

    @Test
    fun cancellationIsNotConvertedIntoFeatureFailure() = runBlocking {
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(request: WeatherRequest, feature: WeatherFeature): FeatureOutcome {
                    throw CancellationException("cancelled by caller")
                }
            },
            clock = object : WeatherClock {
                override fun nowEpochMillis(): Long = 1_500
            },
        )
        assertFailsWith<CancellationException> {
            engine.fetch(WeatherContractDemo.sampleRequest())
        }
    }

    @Test
    fun invalidCoordinatesAndEmptyFeatureRequestsAreRejected() {
        assertFailsWith<IllegalArgumentException> { GeoCoordinates(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            WeatherRequest(GeoCoordinates(0.0, 0.0), emptySet())
        }
    }
}
