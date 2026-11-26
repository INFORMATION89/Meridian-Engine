package io.meridian.contractspike

import kotlinx.coroutines.runBlocking

/**
 * JVM entry point used by the standalone Android instrumentation consumer.
 * This belongs to the M0.2 design spike, not the stable engine API.
 */
object AndroidContractSpike {
    @JvmStatic
    fun verifySampleRequest(): String = runBlocking {
        WeatherContractDemo.verifySampleRequest()
    }
}
