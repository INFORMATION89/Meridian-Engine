package io.meridian.probe

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.meridian.probe.db.ProbeDatabase

object IosStorageProbe {
    fun verifyReopen(): Boolean {
        val name = "meridian-m0-1-probe.db"

        val first = NativeSqliteDriver(ProbeDatabase.Schema, name)
        try {
            val database = ProbeDatabase(first)
            database.transaction {
                probeQueries.insertRecord(1, "initial")
                probeQueries.updateRecord("persisted", 1)
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
    }
}
