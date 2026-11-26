package io.meridian.api

/**
 * Experimental public domain contracts for the M0.2 consumer prototype.
 *
 * These types intentionally contain no transport, persistence, provider, or UI types.
 */
enum class WeatherVariable {
    AIR_TEMPERATURE,
    APPARENT_TEMPERATURE,
    RELATIVE_HUMIDITY,
    PRECIPITATION_PROBABILITY,
    WIND_SPEED,
    WIND_DIRECTION,
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0) {
            "latitude must be finite and between -90 and 90"
        }
        require(longitude.isFinite() && longitude in -180.0..180.0) {
            "longitude must be finite and between -180 and 180"
        }
    }
}

class WeatherQuery(
    location: GeoLocation,
    variables: List<WeatherVariable>,
    forecastHours: Int = 24,
) {
    val location: GeoLocation = location
    val variables: List<WeatherVariable> = variables.toList()
    val forecastHours: Int = forecastHours

    init {
        require(this.variables.isNotEmpty()) { "At least one weather variable must be requested" }
        require(forecastHours in 1..240) { "forecastHours must be between 1 and 240" }
        require(this.variables.distinct().size == this.variables.size) { "variables must not contain duplicates" }
    }

    companion object {
        /** Convenience factory used by the Swift consumer prototype. */
        fun airTemperatureAt(latitude: Double, longitude: Double): WeatherQuery =
            WeatherQuery(
                location = GeoLocation(latitude, longitude),
                variables = listOf(WeatherVariable.AIR_TEMPERATURE),
            )
    }
}

enum class WeatherDataKind {
    OBSERVED,
    FORECAST,
    DERIVED,
    CORRECTED,
    ESTIMATED,
}

enum class DataFreshness {
    CURRENT,
    STALE,
    UNKNOWN,
}

enum class DataQuality {
    UNASSESSED,
    ESTIMATED,
    VALIDATED,
    DEGRADED,
}

/** A neutral meteorological value. uncertainty is expressed in the same unit as value. */
data class WeatherDatum(
    val variable: WeatherVariable,
    val value: Double,
    val unit: String,
    val validAtEpochMillis: Long,
    val issuedAtEpochMillis: Long? = null,
    val observedAtEpochMillis: Long? = null,
    val dataKind: WeatherDataKind,
    val sourceId: String,
    val method: String,
    val freshness: DataFreshness,
    val quality: DataQuality,
    val uncertainty: Double? = null,
) {
    init {
        require(value.isFinite()) { "value must be finite" }
        require(unit.isNotBlank()) { "unit must not be blank" }
        require(sourceId.isNotBlank()) { "sourceId must not be blank" }
        require(method.isNotBlank()) { "method must not be blank" }
        require(uncertainty == null || (uncertainty.isFinite() && uncertainty >= 0.0)) {
            "uncertainty must be null or a finite non-negative value"
        }
    }
}

enum class QueryStatus {
    COMPLETE,
    PARTIAL,
    UNAVAILABLE,
}

enum class ComponentIssueCode {
    NO_DATA_SOURCE,
    SOURCE_UNAVAILABLE,
    UNSUPPORTED_VARIABLE,
    STALE_DATA,
}

data class ComponentIssue(
    val variable: WeatherVariable,
    val code: ComponentIssueCode,
    val safeMessage: String,
)

enum class QueryUnavailableReason {
    NO_DATA_SOURCES_CONFIGURED,
}

class WeatherQueryResult(
    val status: QueryStatus,
    values: List<WeatherDatum>,
    issues: List<ComponentIssue>,
    val unavailableReason: QueryUnavailableReason? = null,
) {
    val values: List<WeatherDatum> = values.toList()
    val issues: List<ComponentIssue> = issues.toList()

    init {
        when (status) {
            QueryStatus.COMPLETE -> {
                require(values.isNotEmpty() && issues.isEmpty()) {
                    "A complete result must contain values and no component issues"
                }
                require(unavailableReason == null) {
                    "A complete result cannot have an unavailable reason"
                }
            }
            QueryStatus.PARTIAL -> {
                require(values.isNotEmpty() && issues.isNotEmpty()) {
                    "A partial result must contain both values and component issues"
                }
                require(unavailableReason == null) {
                    "A partial result cannot have an unavailable reason"
                }
            }
            QueryStatus.UNAVAILABLE -> {
                require(values.isEmpty() && issues.isNotEmpty()) {
                    "An unavailable result must have no values and at least one component issue"
                }
                require(unavailableReason != null) {
                    "An unavailable result must identify its reason"
                }
            }
        }
    }
}
