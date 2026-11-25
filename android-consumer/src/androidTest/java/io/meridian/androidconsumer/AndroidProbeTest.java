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
    public void persistsSqliteDataAcrossDriverReopen() {
        assertTrue(AndroidStorageProbe.verifyReopen(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        ));
    }
}
