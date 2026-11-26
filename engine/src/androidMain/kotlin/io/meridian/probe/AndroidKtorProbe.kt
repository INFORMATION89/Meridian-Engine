package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.runBlocking

/** Android Ktor engine probes; fixture traffic must stay on a local CI server. */
object AndroidKtorProbe {
    @JvmStatic
    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Android)
        try { return true } finally { client.close() }
    }

    @JvmStatic
    fun verifyFixtureRequest(url: String): Boolean = runBlocking {
        val client = HttpClient(Android)
        try {
            FixtureHttpProbe.fetch(client, url) == """{"status":"ok","source":"local-fixture"}"""
        } finally {
            client.close()
        }
    }

    @JvmStatic
    fun verifyFixtureFailure(url: String, expectedCode: ProbeErrorCode, timeoutMillis: Long = 5_000): Boolean =
        runBlocking {
            val client = HttpClient(Android)
            try {
                FixtureHttpProbe.fetch(client, url, timeoutMillis)
                false
            } catch (failure: ProbeFailure) {
                failure.code == expectedCode
            } finally {
                client.close()
            }
        }
}
