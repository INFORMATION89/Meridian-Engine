package io.meridian.consumer

import io.meridian.probe.DesktopStorageProbe
import io.meridian.probe.MeridianProbe

fun main() {
    val probe = MeridianProbe()
    check(probe.transform(" Meridian ", 5) == "meridian:5")
    check(DesktopStorageProbe.verifyReopen())
    println("MERIDIAN_MER-0_1_OK ${probe.version}")
}
