package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T : Any> rememberLastNonNull(value: T?): T? {
    var lastValue by remember { mutableStateOf(value) }
    SideEffect {
        if (value != null) lastValue = value
    }
    return value ?: lastValue
}
