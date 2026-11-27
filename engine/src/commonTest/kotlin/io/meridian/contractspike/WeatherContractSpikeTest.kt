@file:OptIn(ExperimentalWeatherContractSpike::class)

package io.meridian.contractspike

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
            observedAtEpochMillis = null,
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
    fun partialFeatureResultIsNotCachedAsAvailable() = runBlocking {
        val store = InMemoryWeatherStore()
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        val partialData = WeatherFeatureData(
            feature = WeatherFeature.CURRENT_CONDITIONS,
            values = listOf(
                WeatherValue(WeatherVariable.AIR_TEMPERATURE, 18.0, WeatherUnit.CELSIUS),
            ),
            provenance = SourceProvenance("fixture-local", "partial-cache-test"),
            issuedAtEpochMillis = null,
            observedAtEpochMillis = 1_450,
            fetchedAtEpochMillis = 1_500,
            validAtEpochMillis = 1_450,
            expiresAtEpochMillis = 2_000,
        )
        var sourceCalls = 0
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(request: WeatherRequest, feature: WeatherFeature): FeatureOutcome {
                    sourceCalls += 1
                    return FeatureOutcome.Partial(
                        partialData,
                        listOf(WeatherProblem(WeatherProblemCode.SOURCE_FAILURE, "One source was unavailable.")),
                    )
                }
            },
            clock = object : WeatherClock {
                override fun nowEpochMillis(): Long = 1_600
            },
            store = store,
        )

        repeat(2) {
            val outcome = engine.fetch(request).outcomes[WeatherFeature.CURRENT_CONDITIONS]
            val partial = assertIs<FeatureOutcome.Partial>(outcome)
            assertTrue(partial.problems.any { it.code == WeatherProblemCode.SOURCE_FAILURE })
        }
        assertEquals(2, sourceCalls)
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
                issuedAtEpochMillis = 400,
                observedAtEpochMillis = null,
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
        assertEquals(400L, partial.data.issuedAtEpochMillis)
        assertEquals(500L, partial.data.fetchedAtEpochMillis)
        assertEquals(800L, partial.data.validAtEpochMillis)
        assertEquals(1_000L, partial.data.expiresAtEpochMillis)
        assertTrue(partial.problems.any { it.code == WeatherProblemCode.STALE_CACHE_USED })
    }

    @Test
    fun forecastLeadIsDerivedFromIssueAndValidTimes() = runBlocking {
        val forecast = WeatherFeatureData(
            feature = WeatherFeature.HOURLY_FORECAST,
            values = listOf(
                WeatherValue(WeatherVariable.AIR_TEMPERATURE, 19.0, WeatherUnit.CELSIUS),
            ),
            provenance = SourceProvenance("fixture-local", "forecast-time-test"),
            issuedAtEpochMillis = 1_000,
            observedAtEpochMillis = null,
            fetchedAtEpochMillis = 1_100,
            validAtEpochMillis = 4_600,
            expiresAtEpochMillis = 7_200,
        )
        assertEquals(3_600L, forecast.forecastLeadMillis)
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
        Unit
    }

    @Test
    fun invalidCoordinatesAndEmptyFeatureRequestsAreRejected() {
        assertFailsWith<IllegalArgumentException> { GeoCoordinates(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            WeatherRequest(GeoCoordinates(0.0, 0.0), emptySet())
        }
    }

    @Test
    fun staleCacheOlderThanTheExplicitLimitIsNotReturned() = runBlocking {
        val store = InMemoryWeatherStore()
        val request = WeatherContractDemo.sampleRequest(
            cachePolicy = CachePolicy.ALLOW_STALE,
            maxStaleAgeMillis = 500,
        ).copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        store.write(
            cacheKey(request),
            cachedCurrent(fetchedAt = 500, expiresAt = 1_000),
        )
        var sourceCalls = 0
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome {
                    sourceCalls += 1
                    return FeatureOutcome.Unavailable(
                        WeatherProblem(WeatherProblemCode.SOURCE_UNAVAILABLE),
                    )
                }
            },
            clock = fixedClock(1_500),
            store = store,
        )

        val outcome = engine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS)

        assertIs<FeatureOutcome.Unavailable>(outcome)
        assertEquals(1, sourceCalls)
    }

    @Test
    fun expiredCacheIsRefreshedInsteadOfReturnedAsStale() = runBlocking {
        val store = InMemoryWeatherStore()
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        store.write(
            cacheKey(request),
            cachedCurrent(fetchedAt = 500, expiresAt = 1_000),
        )
        var sourceCalls = 0
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome {
                    sourceCalls += 1
                    return FeatureOutcome.Available(
                        cachedCurrent(
                            fetchedAt = 1_500,
                            expiresAt = 3_000,
                            sourceId = "fixture-network",
                        ),
                    )
                }
            },
            clock = fixedClock(1_500),
            store = store,
        )

        val outcome = engine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS)
        val available = assertIs<FeatureOutcome.Available>(outcome)

        assertEquals("fixture-network", available.data.provenance.sourceId)
        assertEquals(DataFreshness.FRESH, available.data.freshness)
        assertEquals(1, sourceCalls)
    }

    @Test
    fun cacheReadFailureDoesNotBlockSourceAndIsReported() = runBlocking {
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        val store = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? =
                throw IllegalStateException("fixture read failure")

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) = Unit
            override suspend fun clear() = Unit
        }
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = FeatureOutcome.Available(
                    cachedCurrent(
                        fetchedAt = 1_500,
                        expiresAt = 3_000,
                        sourceId = "fixture-network",
                    ),
                )
            },
            clock = fixedClock(1_500),
            store = store,
        )

        val outcome = engine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS)
        val partial = assertIs<FeatureOutcome.Partial>(outcome)

        assertEquals("fixture-network", partial.data.provenance.sourceId)
        assertTrue(partial.problems.any { it.code == WeatherProblemCode.STORAGE_READ_FAILED })
    }

    @Test
    fun cacheWriteFailurePreservesDataButReportsPartialOutcome() = runBlocking {
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        val store = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? = null

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) {
                throw IllegalStateException("fixture write failure")
            }

            override suspend fun clear() = Unit
        }
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = FeatureOutcome.Available(
                    cachedCurrent(
                        fetchedAt = 1_500,
                        expiresAt = 3_000,
                        sourceId = "fixture-network",
                    ),
                )
            },
            clock = fixedClock(1_500),
            store = store,
        )

        val outcome = engine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS)
        val partial = assertIs<FeatureOutcome.Partial>(outcome)

        assertEquals(18.0, partial.data.values.single().value)
        assertTrue(partial.problems.any { it.code == WeatherProblemCode.STORAGE_WRITE_FAILED })
    }

    @Test
    fun untrustedExceptionMessagesAreNotExposedInWeatherProblems() = runBlocking {
        val sensitiveMessage = "token=secret-value&latitude=19.4326"
        val request = WeatherContractDemo.sampleRequest()
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))

        val transportEngine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = throw IllegalStateException(sensitiveMessage)
            },
            clock = fixedClock(1_500),
        )
        val transportProblem = assertIs<FeatureOutcome.Failed>(
            transportEngine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
        ).problem
        assertEquals(WeatherProblemCode.TRANSPORT_FAILURE, transportProblem.code)
        assertEquals(null, transportProblem.detail)

        val readFailureStore = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? =
                throw IllegalStateException(sensitiveMessage)

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) = Unit
            override suspend fun clear() = Unit
        }
        val readFailureEngine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = FeatureOutcome.Available(
                    cachedCurrent(1_500, 3_000, "fixture-network"),
                )
            },
            clock = fixedClock(1_500),
            store = readFailureStore,
        )
        val readFailure = assertIs<FeatureOutcome.Partial>(
            readFailureEngine.fetch(request.copy(cachePolicy = CachePolicy.PREFER_FRESH))
                .outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
        ).problems.single { it.code == WeatherProblemCode.STORAGE_READ_FAILED }
        assertEquals(null, readFailure.detail)

        val writeFailureStore = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? = null

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) {
                throw IllegalStateException(sensitiveMessage)
            }

            override suspend fun clear() = Unit
        }
        val writeFailureEngine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = FeatureOutcome.Available(
                    cachedCurrent(1_500, 3_000, "fixture-network"),
                )
            },
            clock = fixedClock(1_500),
            store = writeFailureStore,
        )
        val writeFailure = assertIs<FeatureOutcome.Partial>(
            writeFailureEngine.fetch(request).outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
        ).problems.single { it.code == WeatherProblemCode.STORAGE_WRITE_FAILED }
        assertEquals(null, writeFailure.detail)
    }

    @Test
    fun cancellationDuringCacheReadPropagatesUnchanged() = runBlocking {
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
        val store = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? =
                throw CancellationException("cancelled during cache read")

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) = Unit
            override suspend fun clear() = Unit
        }
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = error("Source must not run after cache-read cancellation")
            },
            clock = fixedClock(1_500),
            store = store,
        )

        assertFailsWith<CancellationException> { engine.fetch(request) }
        Unit
    }

    @Test
    fun cancellationDuringCacheWritePropagatesUnchanged() = runBlocking {
        val request = WeatherContractDemo.sampleRequest(CachePolicy.PREFER_FRESH)
            .copy(features = setOf(WeatherFeature.CURRENT_CONDITIONS))
        val store = object : WeatherStore {
            override suspend fun read(key: FeatureCacheKey): WeatherFeatureData? = null

            override suspend fun write(key: FeatureCacheKey, data: WeatherFeatureData) {
                throw CancellationException("cancelled during cache write")
            }

            override suspend fun clear() = Unit
        }
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome = FeatureOutcome.Available(
                    cachedCurrent(
                        fetchedAt = 1_500,
                        expiresAt = 3_000,
                        sourceId = "fixture-network",
                    ),
                )
            },
            clock = fixedClock(1_500),
            store = store,
        )

        assertFailsWith<CancellationException> { engine.fetch(request) }
        Unit
    }

    @Test
    fun concurrentRequestsOnOneEngineKeepLocationAndCacheResultsIsolated() = runBlocking {
        val bothRequestsStarted = CompletableDeferred<Unit>()
        var sourceCalls = 0
        val store = InMemoryWeatherStore()
        val engine = WeatherContractEngine(
            source = object : WeatherSource {
                override suspend fun fetchFeature(
                    request: WeatherRequest,
                    feature: WeatherFeature,
                ): FeatureOutcome {
                    sourceCalls += 1
                    if (sourceCalls == 2) {
                        bothRequestsStarted.complete(Unit)
                    }
                    withTimeout(5_000) {
                        bothRequestsStarted.await()
                    }

                    val latitude = request.location.latitude
                    return FeatureOutcome.Available(
                        WeatherFeatureData(
                            feature = feature,
                            values = listOf(
                                WeatherValue(WeatherVariable.AIR_TEMPERATURE, latitude, WeatherUnit.CELSIUS),
                            ),
                            provenance = SourceProvenance("fixture-latitude-$latitude", "concurrency-test"),
                            issuedAtEpochMillis = null,
                            observedAtEpochMillis = 1_500,
                            fetchedAtEpochMillis = 1_500,
                            validAtEpochMillis = 1_500,
                            expiresAtEpochMillis = 3_000,
                        ),
                    )
                }
            },
            clock = fixedClock(1_500),
            store = store,
        )
        val feature = setOf(WeatherFeature.CURRENT_CONDITIONS)
        val firstRequest = WeatherContractDemo.sampleRequest()
            .copy(location = GeoCoordinates(19.0, -99.0), features = feature)
        val secondRequest = WeatherContractDemo.sampleRequest()
            .copy(location = GeoCoordinates(20.0, -99.0), features = feature)

        val initialResults = listOf(
            async { engine.fetch(firstRequest) },
            async { engine.fetch(secondRequest) },
        ).awaitAll()

        val firstData = assertIs<FeatureOutcome.Available>(
            initialResults[0].outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
        ).data
        val secondData = assertIs<FeatureOutcome.Available>(
            initialResults[1].outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
        ).data
        assertEquals(19.0, firstData.values.single().value)
        assertEquals(20.0, secondData.values.single().value)
        assertEquals("fixture-latitude-19.0", firstData.provenance.sourceId)
        assertEquals("fixture-latitude-20.0", secondData.provenance.sourceId)
        assertEquals(2, sourceCalls)

        val cachedFirst = engine.fetch(firstRequest.copy(cachePolicy = CachePolicy.PREFER_FRESH))
        val cachedSecond = engine.fetch(secondRequest.copy(cachePolicy = CachePolicy.PREFER_FRESH))
        assertEquals(
            19.0,
            assertIs<FeatureOutcome.Available>(
                cachedFirst.outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
            ).data.values.single().value,
        )
        assertEquals(
            20.0,
            assertIs<FeatureOutcome.Available>(
                cachedSecond.outcomes.getValue(WeatherFeature.CURRENT_CONDITIONS),
            ).data.values.single().value,
        )
        assertEquals(2, sourceCalls)
    }

    private fun cacheKey(request: WeatherRequest) = FeatureCacheKey(
        request.location.latitude,
        request.location.longitude,
        WeatherFeature.CURRENT_CONDITIONS,
    )

    private fun cachedCurrent(
        fetchedAt: Long,
        expiresAt: Long,
        sourceId: String = "fixture-cache",
    ) = WeatherFeatureData(
        feature = WeatherFeature.CURRENT_CONDITIONS,
        values = listOf(
            WeatherValue(WeatherVariable.AIR_TEMPERATURE, 18.0, WeatherUnit.CELSIUS),
        ),
        provenance = SourceProvenance(sourceId, "contract-test"),
        issuedAtEpochMillis = null,
        observedAtEpochMillis = fetchedAt,
        fetchedAtEpochMillis = fetchedAt,
        validAtEpochMillis = fetchedAt,
        expiresAtEpochMillis = expiresAt,
    )

    private fun fixedClock(now: Long) = object : WeatherClock {
        override fun nowEpochMillis(): Long = now
    }

}
