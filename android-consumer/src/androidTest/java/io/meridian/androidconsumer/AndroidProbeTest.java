package io.meridian.androidconsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.meridian.probe.AndroidStorageProbe;
import io.meridian.probe.MeridianProbe;
import io.meridian.probe.ProbeErrorCode;
import io.meridian.probe.ProbeFailure;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidProbeTest {
    @Test
    public void loadsSharedProbeAndMapsTypedFailure() {
        MeridianProbe probe = new MeridianProbe();
        assertEquals("meridian:11", probe.transform(" Meridian ", 11));

        ProbeFailure failure = assertThrows(
            ProbeFailure.class,
            () -> probe.requireNonEmpty(" ")
        );
        assertEquals(ProbeErrorCode.EMPTY_INPUT, failure.getCode());
    }

    @Test
    public void createsAndClosesAndroidKtorEngine() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyClientLifecycle());
    }

    @Test
    public void requestsLocalFixtureThroughAndroidEngine() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureRequest(
            "http://10.0.2.2:8765/probe.json"
        ));
    }

    @Test
    public void mapsHttpStatusAndMalformedFixtureResponses() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8765/status/503", ProbeErrorCode.HTTP_STATUS));
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8765/malformed.json", ProbeErrorCode.MALFORMED_RESPONSE));
    }

    @Test
    public void mapsFixtureTimeoutAndTransportFailure() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8765/slow", ProbeErrorCode.TIMEOUT, 50));
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8766/offline", ProbeErrorCode.TRANSPORT));
    }

    @Test
    public void mapsEmptyAndPartialFixtureResponses() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8765/empty.json", ProbeErrorCode.EMPTY_RESPONSE));
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureFailure(
            "http://10.0.2.2:8765/partial.json", ProbeErrorCode.PARTIAL_RESPONSE));
    }

    @Test
    public void propagatesRequestCancellationThroughAndroidEngine() {
        assertTrue(io.meridian.probe.AndroidKtorProbe.verifyFixtureCancellation(
            "http://10.0.2.2:8765/slow"));
    }

    @Test
    public void persistsSqliteDataAcrossDriverReopen() {
        assertTrue(AndroidStorageProbe.verifyReopen(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        ));
    }
}
