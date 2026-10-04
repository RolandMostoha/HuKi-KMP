package hu.mostoha.mobile.kmp.huki.model.domain

data class OktMarker(
    val id: String,
    val location: Location,
    val type: OktMarkerType,
    val name: String,
    val description: String? = null,
)

enum class OktMarkerType {
    STAMP,
    START,
    END,
}
