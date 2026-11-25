package org.meridian.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import org.meridian.spike.Probe
import org.meridian.spike.ProbeException
import org.meridian.spike.db.ProbeDatabase
import java.io.File

public fun main() = runBlocking {
    check(Probe.transform("meridian", 2) == "meridianmeridian")
    check(Probe.asyncTransform("async", 1) == "async")
    try {
        Probe.transform("x", -1)
        error("Expected typed error")
    } catch (_: ProbeException.NegativeCount) {
        // Expected at the public Kotlin boundary.
    }

    val file = File.createTempFile("meridian-m0-1-", ".db")
    file.deleteOnExit()
    val url = "jdbc:sqlite:${file.absolutePath}"
    JdbcSqliteDriver(url).use { driver ->
        ProbeDatabase.Schema.create(driver)
        ProbeDatabase(driver).probeQueries.insertRecord(1, "saved")
    }
    JdbcSqliteDriver(url).use { reopened ->
        check(ProbeDatabase(reopened).probeQueries.selectRecord(1).executeAsOne() == "saved")
    }

    println("${Probe.version}: JVM consumer PASS")
}
