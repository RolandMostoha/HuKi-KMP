package hu.mostoha.mobile.kmp.huki.model.analytics

/**
 * An Analytics user property. [name] must be registered in the Firebase console first before use.
 */
sealed interface UserProperty {
    val name: String
    val value: String?

    data class Supporter(val isSupporter: Boolean) : UserProperty {
        override val name = "supporter"
        override val value = isSupporter.toString()
    }
}
