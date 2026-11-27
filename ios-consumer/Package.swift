// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "MeridianSwiftConsumer",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "MeridianSwiftConsumer", targets: ["MeridianSwiftConsumer"])
    ],
    dependencies: [
        .package(
            url: "https://github.com/rickclephas/KMP-NativeCoroutines.git",
            exact: "1.0.7-spm-async"
        )
    ],
    targets: [
        .binaryTarget(name: "MeridianShared", path: "Frameworks/MeridianShared.xcframework"),
        .target(
            name: "MeridianSwiftConsumer",
            dependencies: ["MeridianShared"],
            linkerSettings: [.linkedLibrary("sqlite3")]
        ),
        .testTarget(
            name: "MeridianSwiftConsumerTests",
            dependencies: [
                "MeridianSwiftConsumer",
                "MeridianShared",
                .product(name: "KMPNativeCoroutinesAsync", package: "KMP-NativeCoroutines")
            ],
            linkerSettings: [.linkedLibrary("sqlite3")]
        )
    ]
)
