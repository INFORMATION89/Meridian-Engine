import XCTest
import MeridianShared
@testable import MeridianSwiftConsumer

final class ProbeUseTests: XCTestCase {
    func testSwiftCanCallSharedKotlinFramework() {
        XCTAssertEqual(ProbeUse.run(), "meridian:13")
        XCTAssertEqual(MeridianProbe().version, "0.1.0")
    }

    func testSqliteDriverCanReopenDatabase() {
        XCTAssertTrue(IosStorageProbe.shared.verifyReopen())
    }
}
