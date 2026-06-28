package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType

fun toPlaceIconRes(osmType: OsmType?): ImageResource =
    when (osmType) {
        OsmType.WAY -> SharedRes.images.ic_place_type_way
        OsmType.RELATION -> SharedRes.images.ic_place_type_relation
        OsmType.NODE, null -> SharedRes.images.ic_place_type_node
    }
