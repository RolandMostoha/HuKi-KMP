package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface GpxFileDay {
    val key: String

    data object Today : GpxFileDay {
        override val key: String = "today"
    }

    data object Yesterday : GpxFileDay {
        override val key: String = "yesterday"
    }

    data class Date(val label: String) : GpxFileDay {
        override val key: String = label
    }
}
