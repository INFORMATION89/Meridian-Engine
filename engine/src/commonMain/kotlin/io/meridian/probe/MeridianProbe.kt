package io.meridian.probe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext

/**
 * Deliberately tiny cross-platform probe. This is not a production weather API.
 */
class MeridianProbe {
    val version: String = "0.1.0"

    fun transform(input: String, seed: Int): String {
        return "${input.trim().lowercase()}:$seed"
    }

    @Throws(ProbeFailure::class)
    fun requireNonEmpty(input: String): String {
        if (input.isBlank()) throw ProbeFailure(ProbeErrorCode.EMPTY_INPUT)
        return input.trim()
    }

    suspend fun transformAsync(input: String, seed: Int): String =
        withContext(Dispatchers.Default) { transform(input, seed) }

    suspend fun awaitCancellation(): String =
        withContext(Dispatchers.Default) { awaitCancellation() }
}

enum class ProbeErrorCode {
    EMPTY_INPUT,
}

class ProbeFailure(val code: ProbeErrorCode) : Exception("Probe failed: $code")
