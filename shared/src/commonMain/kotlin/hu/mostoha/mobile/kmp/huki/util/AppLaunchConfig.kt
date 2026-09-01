package hu.mostoha.mobile.kmp.huki.util

/**
 * Runtime launch configuration, populated by the platform entry point before the first ViewModel is created.
 */
object AppLaunchConfig {
    const val ARG_SKIP_WHATS_NEW = "skipWhatsNew"
    const val ARG_IMPORT_GPX = "importGpx"

    /**
     * When true, the What's New sheet is not shown on launch. Used by E2E tests to get a clean start.
     */
    var skipWhatsNew: Boolean = false

    /**
     * Path of a bundled GPX the app imports on launch.
     */
    var importGpxPath: String? = null
}
