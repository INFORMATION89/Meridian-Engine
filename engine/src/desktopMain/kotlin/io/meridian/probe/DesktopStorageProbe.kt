package io.meridian.probe

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.meridian.probe.db.ProbeDatabase
import java.nio.file.Files

object DesktopStorageProbe {
    fun verifyReopen(): Boolean {
        val file = Files.createTempFile("meridian-m0-1-", ".db").toFile()
        file.delete()
        val url = "jdbc:sqlite:${file.absolutePath}"

        try {
            JdbcSqliteDriver(url).use { first ->
                ProbeDatabase.Schema.create(first)
                val database = ProbeDatabase(first)
                database.transaction {
                    probeQueries.insertRecord(1, "initial")
                    probeQueries.updateRecord("persisted", 1)
                }
            }

            JdbcSqliteDriver(url).use { reopened ->
                return ProbeDatabase(reopened)
                    .probeQueries
                    .selectRecord(1)
                    .executeAsOne() == "persisted"
            }
        } finally {
            file.delete()
        }
    }
}
