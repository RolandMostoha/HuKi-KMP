package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeOutlines
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun String.toLandscapeOutlines(labelsGeoJson: String, hungaryMaskGeoJson: String): LandscapeOutlines {
    val features = Json.parseToJsonElement(this).jsonObject.getValue("features").jsonArray
    val bounds = features.associate { element ->
        val feature = element.jsonObject
        val id = feature.getValue("properties").jsonObject.getValue("id").jsonPrimitive.content
        val coordinates = feature.getValue("geometry").jsonObject.getValue("coordinates")
        id to coordinates.positions().toBoundingBox()
    }
    return LandscapeOutlines(
        geoJson = this,
        bounds = bounds,
        labelsGeoJson = labelsGeoJson,
        hungaryMaskGeoJson = hungaryMaskGeoJson,
    )
}

private fun JsonElement.positions(): List<JsonArray> {
    val array = jsonArray
    return if (array.firstOrNull() is JsonArray) array.flatMap { it.positions() } else listOf(array)
}

private fun List<JsonArray>.toBoundingBox(): BoundingBox {
    val longitudes = map { it[0].jsonPrimitive.double }
    val latitudes = map { it[1].jsonPrimitive.double }
    return BoundingBox(
        north = latitudes.max(),
        east = longitudes.max(),
        south = latitudes.min(),
        west = longitudes.min(),
    )
}
