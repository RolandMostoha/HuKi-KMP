package hu.mostoha.mobile.kmp.huki

import android.content.Context
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

actual class Strings(val context: Context) {
    actual fun get(id: StringResource, args: List<Any>): String =
        if (args.isEmpty()) {
            StringDesc.Resource(id).toString(context)
        } else {
            StringDesc.ResourceFormatted(id, args).toString(context)
        }
}
