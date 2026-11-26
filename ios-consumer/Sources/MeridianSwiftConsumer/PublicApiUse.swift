import MeridianShared

/// Swift-facing proof of the experimental, consumer-neutral M0.2 API.
public enum PublicApiUse {
    public static func run(completion: @escaping (String?, Error?) -> Void) {
        let engine = MeridianEngine.companion.create()
        let query = WeatherQuery.companion.airTemperatureAt(
            latitude: 19.4326,
            longitude: -99.1332
        )

        engine.query(query: query) { result, error in
            defer { engine.close() }
            if let error {
                completion(nil, error)
                return
            }
            guard let result else {
                completion(nil, NSError(
                    domain: "MeridianPublicApiUse",
                    code: 1,
                    userInfo: [NSLocalizedDescriptionKey: "Meridian returned no result"]
                ))
                return
            }
            completion(String(describing: result.status), nil)
        }
    }
}
