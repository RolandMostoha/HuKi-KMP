package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * Where a sandbox GPX came from. [dirName] is the sandbox sub-directory holding it, which keeps the
 * filesystem the single source of truth for the origin.
 */
enum class GpxOrigin(val dirName: String) {
    EXTERNAL("external"),
    ROUTE_PLANNER("routeplanner"),
    ;

    companion object {
        fun fromPath(path: String): GpxOrigin {
            val dirName = path.substringBeforeLast('/').substringAfterLast('/')

            return entries.firstOrNull { it.dirName == dirName } ?: EXTERNAL
        }
    }
}
