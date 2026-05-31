package hu.mostoha.mobile.kmp.huki.model.domain

enum class OsmType {
    NODE,
    WAY,
    RELATION,
    ;

    companion object {
        fun fromString(raw: String?): OsmType? =
            when (raw?.lowercase()) {
                "node", "n" -> NODE
                "way", "w" -> WAY
                "relation", "r" -> RELATION
                else -> null
            }
    }
}
