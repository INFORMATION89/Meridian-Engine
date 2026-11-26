package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/** iOS Darwin Ktor engine probes; fixture traffic must stay on a local CI server. */
object IosKtorProbe {
    suspend fun fetchFixture(url: String, timeoutMillis: Long = 5_000): String {
        val client = HttpClient(Darwin)
        try { return FixtureHttpProbe.fetch(client, url, timeoutMillis) } finally { client.close() }
    }

    suspend fun verifyFixtureFailure(url: String, expectedCode: ProbeErrorCode, timeoutMillis: Long = 5_000): Boolean {
        val client = HttpClient(Darwin)
        try {
            FixtureHttpProbe.fetch(client, url, timeoutMillis)
            return false
        } catch (failure: ProbeFailure) {
            return failure.code == expectedCode
        } finally {
            client.close()
        }
    }

    suspend fun verifyEmptyResponseFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.EMPTY_RESPONSE)

    suspend fun verifyPartialResponseFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.PARTIAL_RESPONSE)

    suspend fun verifyFixtureCancellation(url: String): Boolean {
        val client = HttpClient(Darwin)
        try { return FixtureHttpProbe.verifyCancellation(client, url) } finally { client.close() }
    }

    suspend fun verifyHttpStatusFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.HTTP_STATUS)

    suspend fun verifyMalformedResponseFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.MALFORMED_RESPONSE)

    suspend fun verifyTimeoutFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.TIMEOUT, 50)

    suspend fun verifyTransportFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.TRANSPORT)

    suspend fun verifyEmptyResponseFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.EMPTY_RESPONSE)

    suspend fun verifyPartialResponseFailure(url: String): Boolean =
        verifyFixtureFailure(url, ProbeErrorCode.PARTIAL_RESPONSE)

    suspend fun verifyFixtureCancellation(url: String): Boolean {
        val client = HttpClient(Darwin)
        try { return FixtureHttpProbe.verifyCancellation(client, url) } finally { client.close() }
    }

    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Darwin)
        try { return true } finally { client.close() }
    }
}
