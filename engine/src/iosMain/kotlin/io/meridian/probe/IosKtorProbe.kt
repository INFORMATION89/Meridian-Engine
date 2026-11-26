package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/** iOS Darwin Ktor engine probes; fixture traffic must stay on a local CI server. */
object IosKtorProbe {
    suspend fun fetchFixture(url: String): String {
        val client = HttpClient(Darwin)
        try { return client.get(url).bodyAsText() } finally { client.close() }
    }

    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Darwin)
        try { return true } finally { client.close() }
    }
}
