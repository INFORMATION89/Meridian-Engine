package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking

/** Android Ktor engine probes; fixture traffic must stay on a local CI server. */
object AndroidKtorProbe {
    private const val EXPECTED_FIXTURE = """{"status":"ok","source":"local-fixture"}"""

    @JvmStatic
    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Android)
        try { return true } finally { client.close() }
    }

    @JvmStatic
    fun verifyFixtureRequest(url: String): Boolean = runBlocking {
        val client = HttpClient(Android)
        try { client.get(url).bodyAsText() == EXPECTED_FIXTURE } finally { client.close() }
    }
}
