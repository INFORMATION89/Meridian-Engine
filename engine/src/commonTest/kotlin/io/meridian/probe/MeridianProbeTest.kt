package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
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
}
