package io.meridian.probe

import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopStorageProbeTest {
    @Test
    fun sqliteDataSurvivesDriverReopen() {
        assertTrue(DesktopStorageProbe.verifyReopen())
    }
}
