package hu.mostoha.mobile.kmp.huki

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

object TestContext {
    internal val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    internal val instrumentationContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context
}
