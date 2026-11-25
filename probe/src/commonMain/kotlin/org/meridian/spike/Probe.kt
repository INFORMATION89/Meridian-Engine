package org.meridian.spike

import kotlinx.coroutines.delay

public object Probe {
    public const val version: String = "m0.1"

    @Throws(ProbeException::class)
    public fun transform(input: String, count: Int): String {
        if (count < 0) throw ProbeException.NegativeCount
        return input.repeat(count)
    }

    public suspend fun asyncTransform(input: String, count: Int): String {
        delay(1)
        return transform(input, count)
    }
}

public sealed class ProbeException : IllegalArgumentException() {
    public data object NegativeCount : ProbeException()
}
