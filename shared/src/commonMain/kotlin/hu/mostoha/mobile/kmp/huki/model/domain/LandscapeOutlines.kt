package hu.mostoha.mobile.kmp.huki.model.domain

data class LandscapeOutlines(
    /**
     * FeatureCollection with one feature per landscape, `properties.id` = [Landscape.id].
     */
    val geoJson: String,
    val bounds: Map<String, BoundingBox>,
    /**
     * One Point per landscape, `properties.id` = [Landscape.id], so split landscapes are named once.
     */
    val labelsGeoJson: String,
    /**
     * World polygon with Hungary's border as its hole, for dimming the map outside the country.
     */
    val hungaryMaskGeoJson: String,
)
