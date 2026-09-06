package hu.mostoha.mobile.kmp.huki

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class StringsTest {

    @Test
    fun givenSingleArgumentPattern_whenResolving_thenArgumentIsSubstituted() {
        val actual = Strings(appContext).get(SharedRes.strings.travel_time_minutes_pattern, listOf(45L))

        actual shouldContain "45"
        actual shouldNotContain "%s"
    }

    @Test
    fun givenTwoArgumentNonPositionalPattern_whenResolving_thenBothArgumentsAreSubstituted() {
        val actual = Strings(appContext).get(SharedRes.strings.travel_time_hours_minutes_pattern, listOf(2L, 15L))

        actual shouldContain "2"
        actual shouldContain "15"
        actual shouldNotContain "%s"
    }
}
