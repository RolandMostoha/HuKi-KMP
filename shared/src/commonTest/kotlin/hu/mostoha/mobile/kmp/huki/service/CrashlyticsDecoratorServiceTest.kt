package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import kotlin.test.Test

class CrashlyticsDecoratorServiceTest {

    private val delegate = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()
    private val service = CrashlyticsDecoratorService(delegate, crashlyticsService)

    @Test
    fun `Given ScreenView event, when logEvent, then screen breadcrumb and custom key are recorded`() {
        val event = AnalyticsEvent.ScreenView(Screen.DESTINATIONS)

        service.logEvent(event)

        crashlyticsService.logs shouldContainExactly listOf("screen: ${Screen.DESTINATIONS.value}")
        crashlyticsService.customKeys shouldContainExactly mapOf(
            CrashlyticsService.CRASH_KEY_LAST_SCREEN to Screen.DESTINATIONS.value,
        )
    }

    @Test
    fun `Given ScreenView event, when logEvent, then event is forwarded to the delegate`() {
        val event = AnalyticsEvent.ScreenView(Screen.SETTINGS)

        service.logEvent(event)

        delegate.screenViews shouldContainExactly listOf(event)
    }

    @Test
    fun `Given non-ScreenView event, when logEvent, then no breadcrumb is recorded but event is forwarded`() {
        val event = AnalyticsEvent.SearchOpened

        service.logEvent(event)

        crashlyticsService.logs.shouldBeEmpty()
        crashlyticsService.customKeys.shouldBeEmpty()
        delegate.loggedEvents shouldContainExactly listOf(event)
    }
}
