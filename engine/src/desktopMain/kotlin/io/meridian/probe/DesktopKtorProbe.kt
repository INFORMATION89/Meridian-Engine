package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

/** Desktop JVM Ktor engine probes; fixture traffic must stay on a local CI server. */
object DesktopKtorProbe {
    suspend fun fetchFixture(url: String, timeoutMillis: Long = 5_000): String {
        val client = HttpClient(CIO)
        try { return FixtureHttpProbe.fetch(client, url, timeoutMillis) } finally { client.close() }
    }

    suspend fun verifyFixtureFailure(url: String, expectedCode: ProbeErrorCode, timeoutMillis: Long = 5_000): Boolean {
        val client = HttpClient(CIO)
        try {
            FixtureHttpProbe.fetch(client, url, timeoutMillis)
            return false
        } catch (failure: ProbeFailure) {
            return failure.code == expectedCode
        } finally {
            client.close()
        }
    }

    suspend fun verifyFixtureCancellation(url: String): Boolean {
        val client = HttpClient(CIO)
        try { return FixtureHttpProbe.verifyCancellation(client, url) } finally { client.close() }
    }

    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(CIO)
        try { return true } finally { client.close() }
    }
}
