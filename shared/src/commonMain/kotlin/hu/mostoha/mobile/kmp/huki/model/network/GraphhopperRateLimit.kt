package hu.mostoha.mobile.kmp.huki.model.network

import kotlin.time.Duration

data class GraphhopperRateLimit(
    val remaining: Int?,
    val resetIn: Duration?,
)
