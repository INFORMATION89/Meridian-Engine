package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import java.io.IOException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MeridianProbeTest {
    @Test
    fun deterministicOperationAndTypedFailure() {
        val probe = MeridianProbe()
        assertEquals("meridian:7", probe.transform(" Meridian ", 7))
        assertEquals(ProbeErrorCode.EMPTY_INPUT, assertFailsWith<ProbeFailure> {
            probe.requireNonEmpty("  ")
        }.code)
    }

    @Test
    fun asynchronousOperationCompletes() = runBlocking {
        assertEquals("fixture:3", MeridianProbe().transformAsync("Fixture", 3))
    }

    @Test
    fun cancellationStopsSuspendedOperation() = runBlocking {
        val job = launch { MeridianProbe().awaitCancellation() }
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
    }

    @Test
    fun ktorFixtureRequestUsesMockEngine() = runBlocking {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = """{"status":"ok","source":"local-fixture"}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
        try {
            assertEquals(
                """{"status":"ok","source":"local-fixture"}""",
                client.get("https://fixture.invalid/probe").bodyAsText()
            )
        } finally {
            client.close()
        }
    }
    @Test
    fun fixtureProbeMapsHttpStatusAndMalformedPayload() = runBlocking {
        val statusClient = HttpClient(MockEngine) {
            engine { addHandler { respond("unavailable", status = HttpStatusCode.ServiceUnavailable) } }
        }
        try {
            val failure = assertFailsWith<ProbeFailure> {
                FixtureHttpProbe.fetch(statusClient, "https://fixture.invalid/status")
            }
            assertEquals(ProbeErrorCode.HTTP_STATUS, failure.code)
        } finally {
            statusClient.close()
        }

        val malformedClient = HttpClient(MockEngine) {
            engine { addHandler { respond("""{"status":""") } }
        }
        try {
            val failure = assertFailsWith<ProbeFailure> {
                FixtureHttpProbe.fetch(malformedClient, "https://fixture.invalid/malformed")
            }
            assertEquals(ProbeErrorCode.MALFORMED_RESPONSE, failure.code)
        } finally {
            malformedClient.close()
        }
    }

    @Test
    fun fixtureProbeMapsTransportFailure() = runBlocking {
        val client = HttpClient(MockEngine) {
            engine { addHandler { throw IOException("fixture offline") } }
        }
        try {
            val failure = assertFailsWith<ProbeFailure> {
                FixtureHttpProbe.fetch(client, "https://fixture.invalid/offline")
            }
            assertEquals(ProbeErrorCode.TRANSPORT, failure.code)
        } finally {
            client.close()
        }
    }

    @Test
    fun fixtureProbeEnforcesTimeout() = runBlocking {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    delay(250)
                    respond("""{"status":"ok","source":"local-fixture"}""")
                }
            }
        }
        try {
            val failure = assertFailsWith<ProbeFailure> {
                FixtureHttpProbe.fetch(client, "https://fixture.invalid/slow", timeoutMillis = 25)
            }
            assertEquals(ProbeErrorCode.TIMEOUT, failure.code)
        } finally {
            client.close()
        }
    }

    @Test
    fun fixtureProbePropagatesCallerCancellation() = runBlocking {
        val client = HttpClient(MockEngine) {
            engine { addHandler { awaitCancellation() } }
        }
        try {
            val job = launch {
                FixtureHttpProbe.fetch(client, "https://fixture.invalid/cancel")
            }
            delay(25)
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
        } finally {
            client.close()
        }
    }

}
