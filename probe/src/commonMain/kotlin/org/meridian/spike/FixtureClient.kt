package org.meridian.spike

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public fun fixtureClient(engine: io.ktor.client.engine.HttpClientEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

public suspend fun readFixture(client: HttpClient, url: String): Fixture = client.get(url).body()

@Serializable
public data class Fixture(val value: String, val count: Int)
