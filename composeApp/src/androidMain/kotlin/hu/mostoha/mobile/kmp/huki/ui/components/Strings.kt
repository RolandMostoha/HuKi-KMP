package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.kmp.huki.Strings

@Composable
fun stringResource(id: StringResource, vararg args: Any): String = Strings(LocalContext.current).get(id, args.toList())
