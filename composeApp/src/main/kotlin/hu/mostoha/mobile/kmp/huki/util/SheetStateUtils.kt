package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val NearlyExpandedThreshold = 24.dp

/**
 * True once a full-height sheet is within [threshold] of its top, before the spring settles its `currentValue`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetState.rememberIsNearlyExpanded(threshold: Dp = NearlyExpandedThreshold): Boolean {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val isNearlyExpanded by remember(this, thresholdPx) {
        derivedStateOf {
            val offset = runCatching { requireOffset() }.getOrNull()
            val isNearTop = offset != null && offset <= thresholdPx
            targetValue == SheetValue.Expanded && (isNearTop || currentValue == SheetValue.Expanded)
        }
    }
    return isNearlyExpanded
}
