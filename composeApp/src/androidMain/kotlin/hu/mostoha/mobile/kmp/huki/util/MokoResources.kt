package hu.mostoha.mobile.kmp.huki.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.Strings

@Composable
fun mokoString(id: StringResource, vararg args: Any): String = Strings(LocalContext.current).get(id, args.toList())

@Composable
fun mokoString(desc: StringDesc): String = Strings(LocalContext.current).get(desc)

@Composable
fun mokoImage(id: ImageResource): ImageVector = ImageVector.vectorResource(id.drawableResId)

fun ColorResource.toComposeColor(context: Context): Color = Color(this.getColor(context))
