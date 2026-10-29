package hu.mostoha.mobile.kmp.huki.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.Strings
import hu.mostoha.mobile.kmp.huki.theme.LocalThemedContext

@Composable
fun mokoString(id: StringResource, vararg args: Any): String = Strings(LocalContext.current).get(id, args.toList())

@Composable
fun mokoString(desc: StringDesc): String = Strings(LocalContext.current).get(desc)

@Composable
fun mokoColor(id: ColorResource): Color = id.toComposeColor(LocalThemedContext.current ?: LocalContext.current)

@Composable
fun mokoImage(id: ImageResource): ImageVector {
    val context = LocalThemedContext.current ?: LocalContext.current
    return remember(context, id) { ImageVector.vectorResource(context.theme, context.resources, id.drawableResId) }
}

fun ColorResource.toComposeColor(context: Context): Color = Color(this.getColor(context))

fun Context.resolveMoko(id: StringResource, vararg args: Any): String = Strings(this).get(id, args.toList())
