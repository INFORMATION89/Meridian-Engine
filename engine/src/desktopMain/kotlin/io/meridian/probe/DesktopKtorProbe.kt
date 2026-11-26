package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/** Desktop JVM Ktor engine probes; fixture traffic must stay on a local CI server. */
object DesktopKtorProbe {
    suspend fun fetchFixture(url: String): String {
        val client = HttpClient(CIO)
        try { return client.get(url).bodyAsText() } finally { client.close() }
    }

    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(CIO)
        try { return true } finally { client.close() }
    }
}
