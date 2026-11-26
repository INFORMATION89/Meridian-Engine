package io.meridian.probe

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopStorageProbeTest {
    @Test
    fun sqliteDataSurvivesDriverReopen() {
        assertTrue(DesktopStorageProbe.verifyReopen())
        assertTrue(DesktopKtorProbe.verifyClientLifecycle())
    }

    @Test
    fun cioEngineRequestsLocalFixture() = runBlocking {
        assertEquals(
            """{"status":"ok","source":"local-fixture"}""",
            DesktopKtorProbe.fetchFixture("http://127.0.0.1:8765/probe.json")
        )
    }
}
