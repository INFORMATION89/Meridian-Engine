package io.meridian.probe

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.meridian.probe.db.ProbeDatabase

object AndroidStorageProbe {
    @JvmStatic
    fun verifyReopen(context: Context): Boolean {
        val name = "meridian-m0-1-probe.db"
        context.deleteDatabase(name)
        val first = AndroidSqliteDriver(ProbeDatabase.Schema, context, name)
        ProbeDatabase(first).probeQueries.insertRecord(1, "persisted")
        first.close()

        val reopened = AndroidSqliteDriver(ProbeDatabase.Schema, context, name)
        val value = ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne()
        reopened.close()
        context.deleteDatabase(name)
        return value == "persisted"
    }
}
