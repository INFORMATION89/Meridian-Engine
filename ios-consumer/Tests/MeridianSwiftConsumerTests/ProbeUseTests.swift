import XCTest
import MeridianShared
@testable import MeridianSwiftConsumer

final class ProbeUseTests: XCTestCase {
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

    func testDarwinEngineRequestsLocalFixture() {
        let finished = expectation(description: "Darwin Ktor engine receives local fixture")
        IosKtorProbe.shared.fetchFixture(url: "http://localhost:8765/probe.json") { body, error in
            XCTAssertNil(error)
            XCTAssertEqual(body, #"{"status":"ok","source":"local-fixture"}"#)
            finished.fulfill()
        }
        wait(for: [finished], timeout: 10)
    }

    func testDarwinEngineMapsHttpAndMalformedResponses() {
        let status = expectation(description: "Darwin maps HTTP status")
        IosKtorProbe.shared.verifyHttpStatusFailure(url: "http://localhost:8765/status/503") { value, error in
            XCTAssertNil(error)
            XCTAssertEqual(value?.boolValue, true)
            status.fulfill()
        }

        let malformed = expectation(description: "Darwin maps malformed payload")
        IosKtorProbe.shared.verifyMalformedResponseFailure(url: "http://localhost:8765/malformed.json") { value, error in
            XCTAssertNil(error)
            XCTAssertEqual(value?.boolValue, true)
            malformed.fulfill()
        }
        wait(for: [status, malformed], timeout: 10)
    }

    func testDarwinEngineMapsTimeoutAndTransportFailures() {
        let timeout = expectation(description: "Darwin maps request timeout")
        IosKtorProbe.shared.verifyTimeoutFailure(url: "http://localhost:8765/slow") { value, error in
            XCTAssertNil(error)
            XCTAssertEqual(value?.boolValue, true)
            timeout.fulfill()
        }

        let transport = expectation(description: "Darwin maps connection failure")
        IosKtorProbe.shared.verifyTransportFailure(url: "http://127.0.0.1:8766/offline") { value, error in
            XCTAssertNil(error)
            XCTAssertEqual(value?.boolValue, true)
            transport.fulfill()
        }
        wait(for: [timeout, transport], timeout: 10)
    }

    func testSqliteDriverCanReopenDatabase() {
        XCTAssertTrue(IosStorageProbe.shared.verifyReopen())
    }
}
