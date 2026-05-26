package hu.mostoha.mobile.kmp.huki.util

import kotlin.time.Duration

fun Duration.millis(): Int = this.inWholeMilliseconds.toInt()
