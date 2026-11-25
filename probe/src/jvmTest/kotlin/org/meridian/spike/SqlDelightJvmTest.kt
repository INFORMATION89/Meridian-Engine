package org.meridian.spike

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.meridian.spike.db.ProbeDatabase
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlDelightJvmTest {
    @Test
    fun cioEngineLoadsOnDesktopJvm() {
        HttpClient(CIO).use { }
    }

    @Test
    fun createsWritesReadsAndReopensDatabase() {
        val file = File.createTempFile("meridian-probe-", ".db")
        file.deleteOnExit()
        val url = "jdbc:sqlite:${file.absolutePath}"
        JdbcSqliteDriver(url).use { driver ->
            ProbeDatabase.Schema.create(driver)
            ProbeDatabase(driver).probeQueries.insertRecord(1, "persisted")
        }
        JdbcSqliteDriver(url).use { reopened ->
            assertEquals("persisted", ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne())
        }
    }
}
