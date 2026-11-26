package io.meridian.consumer

import io.meridian.probe.DesktopKtorProbe
import io.meridian.probe.DesktopStorageProbe
import io.meridian.probe.MeridianProbe
import io.meridian.probe.ProbeErrorCode

suspend fun main() {
    val probe = MeridianProbe()
    check(probe.transform(" Meridian ", 5) == "meridian:5")
    check(DesktopKtorProbe.verifyClientLifecycle())
    check(
        DesktopKtorProbe.fetchFixture("http://127.0.0.1:8765/probe.json") ==
            """{"status":"ok","source":"local-fixture"}"""
    )
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/status/503", ProbeErrorCode.HTTP_STATUS))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/malformed.json", ProbeErrorCode.MALFORMED_RESPONSE))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/slow", ProbeErrorCode.TIMEOUT, 50))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8766/offline", ProbeErrorCode.TRANSPORT))
    check(DesktopStorageProbe.verifyReopen())
    println("MERIDIAN_MER-0_1_OK ${probe.version}")
}
