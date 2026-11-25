package org.meridian.spike

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProbeTest {
    @Test
    fun deterministicTransformAndTypedError() {
        assertEquals("abab", Probe.transform("ab", 2))
        assertFailsWith<ProbeException.NegativeCount> { Probe.transform("x", -1) }
    }

    @Test
    fun asyncOperationAndCancellation() = runTest {
        assertEquals("xxx", Probe.asyncTransform("x", 3))
        val job = launch(start = CoroutineStart.UNDISPATCHED) { Probe.asyncTransform("cancel", 1) }
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
    }

    @Test
    fun ktorReadsSerializedFixtureWithoutNetwork() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"value":"fixture","count":2}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = fixtureClient(engine)
        try {
            assertEquals(Fixture("fixture", 2), readFixture(client, "https://fixture.invalid/probe"))
        } finally {
            client.close()
        }
    }
}
