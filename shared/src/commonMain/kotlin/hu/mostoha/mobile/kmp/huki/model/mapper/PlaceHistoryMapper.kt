package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.db.PlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.util.NameNormalizer
import kotlin.time.Instant

fun Place.toPlaceHistoryEntity(visitedAt: Long): PlaceHistoryEntity =
    PlaceHistoryEntity(
        osmType = osmType ?: OsmType.NODE,
        osmId = osmId,
        name = name,
        nameNormalized = NameNormalizer.normalize(name),
        address = address,
        latitude = location.latitude,
        longitude = location.longitude,
        placeCategory = placeCategory,
        placeSource = placeSource,
        boundingBox = boundingBox,
        lastVisited = visitedAt,
    )

fun PlaceHistoryEntity.toPlace(): Place =
    Place(
        osmId = osmId,
        location = Location(latitude, longitude),
        name = name,
        placeSource = placeSource,
        address = address,
        placeCategory = placeCategory,
        osmType = osmType,
        boundingBox = boundingBox,
    )

fun PlaceHistoryEntity.toPlaceHistoryItem(): PlaceHistoryItem =
    PlaceHistoryItem(
        place = toPlace(),
        lastVisited = Instant.fromEpochMilliseconds(lastVisited),
    )
