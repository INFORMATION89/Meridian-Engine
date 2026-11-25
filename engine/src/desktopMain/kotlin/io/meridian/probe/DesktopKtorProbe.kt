package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

/**
 * Verifies that the CIO Ktor engine can be created and closed
 * on the Desktop JVM runtime. This does not make a network request.
 */
object DesktopKtorProbe {
    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(CIO)
        try {
            return true
        } finally {
            client.close()
        }
    }
}
