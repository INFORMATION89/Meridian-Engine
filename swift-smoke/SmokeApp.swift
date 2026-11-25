import Foundation
import MeridianProbe
import SwiftUI

@main
struct MeridianSmokeApp: App {
    init() {
        do {
            let output = try Probe.shared.transform(input: "ios", count: 2)
            precondition(output == "iosios")

            do {
                _ = try Probe.shared.transform(input: "x", count: -1)
                fatalError("Expected the typed Kotlin error to cross the Swift boundary")
            } catch {
                precondition(error is NSError)
            }

            Probe.shared.asyncTransform(input: "async", count: 1) { output, error in
                guard error == nil, output == "async" else {
                    fatalError("The asynchronous Kotlin call failed: \(String(describing: error))")
                }
                let marker = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
                    .appendingPathComponent("m0-1-result.txt")
                try? "PASS".write(to: marker, atomically: true, encoding: .utf8)
            }
        } catch {
            fatalError("The Kotlin probe failed: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup { Text("Meridian M0.1") }
    }
}
