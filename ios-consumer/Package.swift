// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "MeridianSwiftConsumer",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "MeridianSwiftConsumer", targets: ["MeridianSwiftConsumer"])
    ],
    targets: [
        .binaryTarget(name: "MeridianShared", path: "Frameworks/MeridianShared.xcframework"),
        .target(name: "MeridianSwiftConsumer", dependencies: ["MeridianShared"]),
        .testTarget(name: "MeridianSwiftConsumerTests", dependencies: ["MeridianSwiftConsumer", "MeridianShared"])
    ]
)
