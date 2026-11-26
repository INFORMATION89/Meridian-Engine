package io.meridian.probe

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * M0.1 consumer-boundary contract for a deliberately tiny, deterministic HTTP fixture.
 *
 * This is evidence code, not Meridian's future production networking API. Callers own
 * the HttpClient lifecycle; cancellation is propagated and never translated to I/O failure.
 */
object FixtureHttpProbe {
    private val fixturePattern = Regex(
        """^\s*\{\s*"status"\s*:\s*"ok"\s*,\s*"source"\s*:\s*"local-fixture"\s*\}\s*$"""
    )
    private val partialPattern = Regex(
        """^\s*\{\s*(?:"status"\s*:\s*"ok"|"source"\s*:\s*"local-fixture")\s*\}\s*$"""
    )

    suspend fun fetch(
        client: HttpClient,
        url: String,
        timeoutMillis: Long = 5_000
    ): String {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        try {
            return withTimeout(timeoutMillis) {
                val response = client.get(url)
                if (!response.status.isSuccess()) {
                    throw ProbeFailure(ProbeErrorCode.HTTP_STATUS, "HTTP " + response.status.value)
                }
                val body = response.bodyAsText()
                if (body.isBlank()) {
                    throw ProbeFailure(ProbeErrorCode.EMPTY_RESPONSE, "Fixture response body was empty")
                }
                if (!fixturePattern.matches(body)) {
                    val code = if (partialPattern.matches(body)) {
                        ProbeErrorCode.PARTIAL_RESPONSE
                    } else {
                        ProbeErrorCode.MALFORMED_RESPONSE
                    }
                    throw ProbeFailure(code, "Unexpected fixture response")
                }
                body
            }
        } catch (failure: ProbeFailure) {
            throw failure
        } catch (timeout: TimeoutCancellationException) {
            throw ProbeFailure(ProbeErrorCode.TIMEOUT, "Fixture request exceeded " + timeoutMillis + "ms")
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (transport: Exception) {
            throw ProbeFailure(ProbeErrorCode.TRANSPORT, transport.message ?: "Fixture transport failed")
        }
    }

    suspend fun verifyCancellation(client: HttpClient, url: String): Boolean = coroutineScope {
        val request = launch { fetch(client, url) }
        delay(25)
        request.cancelAndJoin()
        request.isCancelled
    }
}
