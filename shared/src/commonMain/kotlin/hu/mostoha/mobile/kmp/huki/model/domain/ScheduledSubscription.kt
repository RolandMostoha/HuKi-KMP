package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.time.Instant

data class ScheduledSubscription(
    val animal: SupportAnimal,
    val startTime: Instant,
)
