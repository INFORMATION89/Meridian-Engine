package org.meridian.smoke

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.meridian.spike.Probe
import org.meridian.spike.ProbeException
import org.meridian.spike.db.ProbeDatabase

@RunWith(AndroidJUnit4::class)
class ProbeInstrumentedTest {
    @Test
    fun appLoadsSharedProbeAndMapsTypedError() {
        assertEquals("m0.1", Probe.version)
        assertEquals("okok", Probe.transform("ok", 2))
        assertThrows(ProbeException.NegativeCount::class.java) { Probe.transform("x", -1) }
    }

    @Test
    fun asyncOperationReturnsAndPropagatesCancellation() = runBlocking {
        assertEquals("async", Probe.asyncTransform("async", 1))
        assertEquals(null, withTimeoutOrNull(0) { Probe.asyncTransform("cancel", 1) })
    }

    @Test
    fun androidDriverPersistsAcrossReopen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "meridian-m0-1-${System.nanoTime()}.db"
        try {
            AndroidSqliteDriver(ProbeDatabase.Schema, context, name).use { driver ->
                ProbeDatabase(driver).probeQueries.insertRecord(1, "android")
            }
            AndroidSqliteDriver(ProbeDatabase.Schema, context, name).use { reopened ->
                assertEquals("android", ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne())
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun androidAndCioEnginesLoadOnDevice() {
        HttpClient(Android).use { }
        HttpClient(CIO).use { }
    }
}
