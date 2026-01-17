package hu.mostoha.mobile.kmp.huki.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.kmp.huki.Strings

@Composable
fun mokoString(id: StringResource, vararg args: Any): String = Strings(LocalContext.current).get(id, args.toList())

fun ColorResource.toComposeColor(context: Context): Color = Color(this.getColor(context))
