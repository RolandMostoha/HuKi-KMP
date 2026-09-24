package hu.mostoha.mobile.kmp.huki.theme

object MapLighting {
    /**
     * `*-emissive-strength` for HuKi's own overlays, so they keep their real colours.
     *
     * Every layer HuKi draws on the map needs this. A new layer that omits it will silently dim in dark mode.
     */
    const val OVERLAY_EMISSIVE_STRENGTH = 1.0
}
