package org.meridian.smoke

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import org.meridian.spike.Probe

class SmokeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val output = Probe.transform("ok", 2)
        check(output == "okok")
        setContentView(TextView(this).apply { text = "${Probe.version}:$output" })
    }
}
