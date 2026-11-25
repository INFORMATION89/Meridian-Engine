package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

/**
 * Verifies that the Android-specific Ktor engine can be created and closed
 * inside a real Android runtime. This does not make a network request.
 */
object AndroidKtorProbe {
    @JvmStatic
    fun verifyClientLifecycle(): Boolean {
        val client = HttpClient(Android)
        try {
            return true
        } finally {
            client.close()
        }
    }
}
