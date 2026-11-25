package io.meridian.probe

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.meridian.probe.db.ProbeDatabase

object AndroidStorageProbe {
    @JvmStatic
    fun verifyReopen(context: Context): Boolean {
        val name = "meridian-m0-1-probe.db"
        context.deleteDatabase(name)

        try {
            val first = AndroidSqliteDriver(ProbeDatabase.Schema, context, name)
            try {
                val database = ProbeDatabase(first)
                database.transaction {
                    probeQueries.insertRecord(1, "initial")
                    probeQueries.updateRecord("persisted", 1)
                }
            } finally {
                first.close()
            }

            val reopened = AndroidSqliteDriver(ProbeDatabase.Schema, context, name)
            try {
                return ProbeDatabase(reopened)
                    .probeQueries
                    .selectRecord(1)
                    .executeAsOne() == "persisted"
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
