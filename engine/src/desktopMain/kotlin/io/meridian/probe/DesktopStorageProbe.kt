package io.meridian.probe

import io.meridian.probe.db.ProbeDatabase
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files

object DesktopStorageProbe {
    fun verifyReopen(): Boolean {
        val file = Files.createTempFile("meridian-m0-1-", ".db").toFile()
        file.delete()
        val url = "jdbc:sqlite:${file.absolutePath}"
        try {
            JdbcSqliteDriver(url).use { first ->
                ProbeDatabase.Schema.create(first)
                ProbeDatabase(first).probeQueries.insertRecord(1, "persisted")
            }
            JdbcSqliteDriver(url).use { reopened ->
                return ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne() == "persisted"
            }
        } finally {
            file.delete()
        }
    }
}
