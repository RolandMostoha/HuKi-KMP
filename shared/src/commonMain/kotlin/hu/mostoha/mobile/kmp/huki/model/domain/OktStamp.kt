package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * A stamping point, e.g. [tag] `OKTPH_01_2` has the [number] `1.2`.
 */
data class OktStamp(
    val title: String,
    val description: String,
    val location: Location,
    val tag: String,
    val number: Double,
)
