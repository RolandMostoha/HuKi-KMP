package hu.mostoha.mobile.kmp.huki.model.analytics

enum class GpxShareSource(val value: String) {
    // Share button in the GPX Details sheet
    DETAILS("details"),

    // Share item in the GPX Collection's per-file options menu
    COLLECTION("collection"),
}
