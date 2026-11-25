package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * Verifies that the Darwin Ktor engine can be created and closed
 * in the iOS runtime. This does not make a network request.
 */
object IosKtorProbe {
    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Darwin)
        try {
            return true
        } finally {
            client.close()
        }
    }
}
