package org.meridian.spike

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.meridian.spike.db.ProbeDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlDelightIosTest {
    @Test
    fun darwinEngineLoadsOnSimulator() {
        HttpClient(Darwin).use { }
    }

    @Test
    fun nativeDriverPersistsAcrossReopen() {
        val name = "meridian-probe-${kotlin.random.Random.nextLong()}.db"
        val driver = NativeSqliteDriver(ProbeDatabase.Schema, name)
        try {
            ProbeDatabase(driver).probeQueries.insertRecord(1, "ios")
        } finally {
            driver.close()
        }
        val reopened = NativeSqliteDriver(ProbeDatabase.Schema, name)
        try {
            assertEquals("ios", ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne())
        } finally {
            reopened.close()
        }
    }
}
