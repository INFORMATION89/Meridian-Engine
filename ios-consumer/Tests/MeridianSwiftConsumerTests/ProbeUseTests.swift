import XCTest
import MeridianShared
import KMPNativeCoroutinesAsync
@testable import MeridianSwiftConsumer

final class ProbeUseTests: XCTestCase {
    func testSwiftCanCallSharedKotlinFramework() {
        XCTAssertEqual(ProbeUse.run(), "meridian:13")
        XCTAssertEqual(MeridianProbe().version, "0.1.0")
    }

    func testTypedFailureMapsToSwiftError() {
        XCTAssertThrowsError(try MeridianProbe().requireNonEmpty(input: "  "))
    }

    func testNativeCoroutinesCanAwaitSharedKotlinSuspendFunction() async throws {
        let result = try await asyncFunction(
            for: MeridianProbe().transformAsync(input: "Swift native bridge", seed: 42)
        )
        XCTAssertEqual(result, "swift native bridge:42")
    }

    func testNativeCoroutinesPropagatesSwiftTaskCancellation() async throws {
        let cancellationObserved = expectation(
            description: "Swift task cancellation reaches Kotlin through KMP-NativeCoroutines"
        )

        let operation = Task {
            do {
                _ = try await asyncFunction(for: MeridianProbe().awaitCancellation())
                XCTFail("Expected native coroutine cancellation to reach Kotlin")
                cancellationObserved.fulfill()
            } catch is CancellationError {
                cancellationObserved.fulfill()
            } catch {
                XCTFail("Expected CancellationError, received: \\(error)")
                cancellationObserved.fulfill()
            }
        }

        try await Task.sleep(for: .milliseconds(50))
        operation.cancel()
        await fulfillment(of: [cancellationObserved], timeout: 5)
    }

    func testSuspendOperationCompletesForSwiftConsumer() {
        let finished = expectation(description: "Kotlin suspend operation completes")
        MeridianProbe().transformAsync(input: "Swift", seed: 4) { value, error in
            XCTAssertNil(error)
            XCTAssertEqual(value, "swift:4")
            finished.fulfill()
        }
        wait(for: [finished], timeout: 5)
    }

    func testDarwinKtorEngineCanBeCreatedAndClosed() {
        XCTAssertTrue(IosKtorProbe.shared.verifyClientLifecycle())
    }

    func testDarwinEngineRequestsLocalFixture() async throws {
        let body = try await IosKtorProbe.shared.fetchFixture(
            url: "http://localhost:8765/probe.json",
            timeoutMillis: 5_000
        )
        XCTAssertEqual(body, #"{"status":"ok","source":"local-fixture"}"#)
    }

    func testDarwinEngineMapsHttpAndMalformedResponses() async throws {
        let status = try await IosKtorProbe.shared.verifyHttpStatusFailure(
            url: "http://localhost:8765/status/503"
        )
        XCTAssertTrue(status.boolValue)

        let malformed = try await IosKtorProbe.shared.verifyMalformedResponseFailure(
            url: "http://localhost:8765/malformed.json"
        )
        XCTAssertTrue(malformed.boolValue)
    }

    func testDarwinEngineMapsTimeoutAndTransportFailures() async throws {
        let timeout = try await IosKtorProbe.shared.verifyTimeoutFailure(
            url: "http://127.0.0.1:8765/slow"
        )
        XCTAssertTrue(timeout.boolValue)

        let transport = try await IosKtorProbe.shared.verifyTransportFailure(
            url: "http://127.0.0.1:8766/offline"
        )
        XCTAssertTrue(transport.boolValue)
    }

    func testDarwinEngineMapsEmptyAndPartialResponses() async throws {
        let empty = try await IosKtorProbe.shared.verifyEmptyResponseFailure(
            url: "http://localhost:8765/empty.json"
        )
        XCTAssertTrue(empty.boolValue)

        let partial = try await IosKtorProbe.shared.verifyPartialResponseFailure(
            url: "http://localhost:8765/partial.json"
        )
        XCTAssertTrue(partial.boolValue)
    }

    func testDarwinEnginePropagatesRequestCancellation() async throws {
        let cancelled = try await IosKtorProbe.shared.verifyFixtureCancellation(
            url: "http://127.0.0.1:8765/cancel"
        )
        XCTAssertTrue(cancelled.boolValue)
    }

    func testSqliteDriverCanReopenDatabase() {
        XCTAssertTrue(IosStorageProbe.shared.verifyReopen())
    }
}
