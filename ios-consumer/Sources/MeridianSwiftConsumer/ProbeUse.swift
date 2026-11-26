import MeridianShared

public enum ProbeUse {
    public static func run() -> String {
        let probe = MeridianProbe()
        return probe.transform(input: " Meridian ", seed: 13)
    }
}
