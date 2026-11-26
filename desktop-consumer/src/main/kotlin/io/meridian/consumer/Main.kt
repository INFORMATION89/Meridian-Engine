package io.meridian.consumer

import io.meridian.probe.DesktopKtorProbe
import io.meridian.probe.DesktopStorageProbe
import io.meridian.probe.MeridianProbe
import io.meridian.probe.ProbeErrorCode
import io.meridian.contractspike.ExperimentalWeatherContractSpike
import io.meridian.contractspike.WeatherContractDemo

@OptIn(ExperimentalWeatherContractSpike::class)
suspend fun main() {
    val contractSummary = WeatherContractDemo.verifySampleRequest()
    check(contractSummary == "current=available;hourly=unavailable;air=18.0C;apparent=21.0C")
    val probe = MeridianProbe()
    check(probe.transform(" Meridian ", 5) == "meridian:5")
    check(DesktopKtorProbe.verifyClientLifecycle())
    check(
        DesktopKtorProbe.fetchFixture("http://127.0.0.1:8765/probe.json") ==
            """{"status":"ok","source":"local-fixture"}"""
    )
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/status/503", ProbeErrorCode.HTTP_STATUS))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/malformed.json", ProbeErrorCode.MALFORMED_RESPONSE))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/empty.json", ProbeErrorCode.EMPTY_RESPONSE))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/partial.json", ProbeErrorCode.PARTIAL_RESPONSE))
    check(DesktopKtorProbe.verifyFixtureCancellation("http://127.0.0.1:8765/cancel"))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8765/slow", ProbeErrorCode.TIMEOUT, 50))
    check(DesktopKtorProbe.verifyFixtureFailure("http://127.0.0.1:8766/offline", ProbeErrorCode.TRANSPORT))
    check(DesktopStorageProbe.verifyReopen())
    println("MERIDIAN_MER-0_1_OK ${probe.version}")
}
