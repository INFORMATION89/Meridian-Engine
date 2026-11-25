package io.meridian.probe

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.meridian.probe.db.ProbeDatabase

object IosStorageProbe {
    fun verifyReopen(): Boolean {
        val name = "meridian-m0-1-probe.db"
        val first = NativeSqliteDriver(ProbeDatabase.Schema, name)
        ProbeDatabase(first).probeQueries.insertRecord(1, "persisted")
        first.close()

        val reopened = NativeSqliteDriver(ProbeDatabase.Schema, name)
        val value = ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne()
        reopened.close()
        return value == "persisted"
    }
}
