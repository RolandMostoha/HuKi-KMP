package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

fun Dp.dpToPx(density: Density) = with(density) { this@dpToPx.toPx() }

fun Int.pxToDp(density: Density) = with(density) { this@pxToDp.toDp() }
