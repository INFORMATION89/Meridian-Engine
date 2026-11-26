package io.meridian.probe

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
import io.meridian.probe.db.ProbeDatabase

object IosStorageProbe {
    fun verifyReopen(): Boolean {
        val name = "meridian-m0-1-probe.db"

        // Keep simulator runs repeatable and prevent stale state from hiding failures.
        DatabaseFileContext.deleteDatabase(name)
        try {
            val first = NativeSqliteDriver(ProbeDatabase.Schema, name)
            try {
                val database = ProbeDatabase(first)
                database.probeQueries.transaction {
                    database.probeQueries.insertRecord(1, "initial")
                    database.probeQueries.updateRecord("persisted", 1)
                }
            } finally {
                first.close()
            }

            val reopened = NativeSqliteDriver(ProbeDatabase.Schema, name)
            try {
                return ProbeDatabase(reopened)
                    .probeQueries
                    .selectRecord(1)
                    .executeAsOne() == "persisted"
            } finally {
                reopened.close()
            }
        } finally {
            // Also remove the database and SQLite sidecar files after assertion failures.
            DatabaseFileContext.deleteDatabase(name)
        }
    }
}
