package hu.mostoha.mobile.kmp.huki.util

private var debugBuild = false

fun setDebugBuild(isDebug: Boolean) {
    debugBuild = isDebug
}

actual val isDebugBuild: Boolean
    get() = debugBuild
