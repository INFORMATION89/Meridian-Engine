package io.meridian.api

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MeridianEngineContractTest {
    @Test
    fun constructsAndReturnsExplicitUnavailableWithoutConfiguredSources() = runBlocking {
        val engine = MeridianEngine.create()
        try {
            val query = WeatherQuery(
                location = GeoLocation(latitude = 19.4326, longitude = -99.1332),
                variables = listOf(
                    WeatherVariable.AIR_TEMPERATURE,
                    WeatherVariable.PRECIPITATION_PROBABILITY,
                ),
            )
            val result = engine.query(query)

            assertEquals(QueryStatus.UNAVAILABLE, result.status)
            assertTrue(result.values.isEmpty())
            assertEquals(2, result.issues.size)
            assertTrue(result.issues.all { it.code == ComponentIssueCode.NO_DATA_SOURCE })
            assertEquals(
                QueryUnavailableReason.NO_DATA_SOURCES_CONFIGURED,
                result.unavailableReason,
            )
        } finally {
            engine.close()
        }
    }

    @Test
    fun closeIsIdempotentAndRejectsLaterQueries() = runBlocking {
        val engine = MeridianEngine.create()
        engine.close()
        engine.close()

        assertFailsWith<MeridianEngineClosedException> {
            runBlocking {
                engine.query(
                    WeatherQuery(
                        location = GeoLocation(0.0, 0.0),
                        variables = listOf(WeatherVariable.AIR_TEMPERATURE),
                    ),
                )
            }
        }
    }

    @Test
    fun validatesCoordinatesAndQueryShapeBeforeExecution() {
        assertFailsWith<IllegalArgumentException> { GeoLocation(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { GeoLocation(0.0, 181.0) }
        assertFailsWith<IllegalArgumentException> {
            WeatherQuery(GeoLocation(0.0, 0.0), emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            WeatherQuery(
                GeoLocation(0.0, 0.0),
                listOf(WeatherVariable.AIR_TEMPERATURE),
                forecastHours = 241,
            )
        }
    }

    @Test
    fun resultEnvelopeRejectsContradictoryStates() {
        assertFailsWith<IllegalArgumentException> {
            WeatherQueryResult(
                status = QueryStatus.COMPLETE,
                values = emptyList(),
                issues = emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            WeatherQueryResult(
                status = QueryStatus.PARTIAL,
                values = emptyList(),
                issues = emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            WeatherQueryResult(
                status = QueryStatus.UNAVAILABLE,
                values = emptyList(),
                issues = emptyList(),
            )
        }
    }

    @Test
    fun queryCopiesRequestedVariables() {
        val requested = mutableListOf(WeatherVariable.AIR_TEMPERATURE)
        val query = WeatherQuery(GeoLocation(0.0, 0.0), requested)
        requested += WeatherVariable.WIND_SPEED

        assertEquals(listOf(WeatherVariable.AIR_TEMPERATURE), query.variables)
    }

    @Test
    fun domainCanRepresentPartialValuesWithProvenanceAndQuality() {
        val datum = WeatherDatum(
            variable = WeatherVariable.AIR_TEMPERATURE,
            value = 21.5,
            unit = "Cel",
            validAtEpochMillis = 1_798_000_000_000,
            dataKind = WeatherDataKind.FORECAST,
            sourceId = "local-fixture-model",
            method = "deterministic-fixture",
            freshness = DataFreshness.CURRENT,
            quality = DataQuality.VALIDATED,
            uncertainty = 1.0,
        )
        val result = WeatherQueryResult(
            status = QueryStatus.PARTIAL,
            values = listOf(datum),
            issues = listOf(
                ComponentIssue(
                    variable = WeatherVariable.PRECIPITATION_PROBABILITY,
                    code = ComponentIssueCode.SOURCE_UNAVAILABLE,
                    safeMessage = "Fixture source did not provide this variable",
                ),
            ),
        )

        assertEquals(QueryStatus.PARTIAL, result.status)
        assertEquals("local-fixture-model", result.values.single().sourceId)
        assertEquals(DataFreshness.CURRENT, result.values.single().freshness)
        assertNull(result.unavailableReason)
    }
}
