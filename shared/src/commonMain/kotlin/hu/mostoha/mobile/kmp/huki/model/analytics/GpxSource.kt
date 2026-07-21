package hu.mostoha.mobile.kmp.huki.model.analytics

enum class GpxSource(val value: String) {
    // External "Open with HuKi" from the OS Files app / Share
    FILES("files"),

    // In-app file picker launched from the Layers -> GPX sheet
    LAYERS("layers"),
}
