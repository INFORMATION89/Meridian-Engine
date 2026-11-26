package io.meridian.consumer

import io.meridian.api.GeoLocation
import io.meridian.api.MeridianEngine
import io.meridian.api.QueryStatus
import io.meridian.api.WeatherQuery
import io.meridian.api.WeatherVariable

/**
 * Independent consumer example: imports only Meridian's experimental public API.
 */
suspend fun runPublicApiContract(): String {
    val engine = MeridianEngine.create()
    try {
        val result = engine.query(
            WeatherQuery(
                location = GeoLocation(19.4326, -99.1332),
                variables = listOf(WeatherVariable.AIR_TEMPERATURE),
            ),
        )
        check(result.status == QueryStatus.UNAVAILABLE)
        check(result.values.isEmpty())
        check(result.issues.size == 1)
        return "PUBLIC_API_OK:" + result.status
    } finally {
        engine.close()
    }
}
