import XCTest
import MeridianShared
@testable import MeridianSwiftConsumer

final class ProbeUseTests: XCTestCase {

    func testSwiftConsumesM02WeatherContractWithoutSolstice() async throws {
        let finished = expectation(description: "M0.2 Kotlin suspend contract returns to Swift")
        let operation = Task {
            do {
                let summary = try await WeatherContractDemo.shared.verifySampleRequest()
                XCTAssertEqual(
                    summary,
                    "current=available;hourly=unavailable;air=18.0C;apparent=21.0C"
                )
                finished.fulfill()
            } catch {
                XCTFail("M0.2 suspend contract failed: \(error)")
                finished.fulfill()
            }
        }

        await fulfillment(of: [finished], timeout: 5)
        operation.cancel()
    }

    func testSwiftCanCallSharedKotlinFramework() {
        XCTAssertEqual(ProbeUse.run(), "meridian:13")
        XCTAssertEqual(MeridianProbe().version, "0.1.0")
    }

    func testTypedFailureMapsToSwiftError() {
        XCTAssertThrowsError(try MeridianProbe().requireNonEmpty(input: "  "))
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
            url: "http://127.0.0.1:8765/probe.json",
            timeoutMillis: 5_000
        )
        XCTAssertEqual(body, #"{"status":"ok","source":"local-fixture"}"#)
    }

    func testDarwinEngineMapsHttpAndMalformedResponses() async throws {
        let status = try await IosKtorProbe.shared.verifyHttpStatusFailure(
            url: "http://127.0.0.1:8765/status/503"
        )
        XCTAssertTrue(status.boolValue)

        let malformed = try await IosKtorProbe.shared.verifyMalformedResponseFailure(
            url: "http://127.0.0.1:8765/malformed.json"
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
        // Keep the same localhost origin used by the passing Apple fixture run.
        let emptyCode = try await IosKtorProbe.shared.fixtureFailureCode(
            url: "http://localhost:8765/empty.json"
        )
        XCTAssertEqual(emptyCode, "EMPTY_RESPONSE", "Unexpected mapping for an empty HTTP body")

        let partialCode = try await IosKtorProbe.shared.fixtureFailureCode(
            url: "http://localhost:8765/partial.json"
        )
        XCTAssertEqual(partialCode, "PARTIAL_RESPONSE", "Unexpected mapping for a partial fixture body")
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
