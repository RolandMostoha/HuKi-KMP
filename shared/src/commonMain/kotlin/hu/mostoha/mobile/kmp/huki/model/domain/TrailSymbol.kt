package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class TrailSymbolSection {
    ROUTE,
    TARGET,
}

enum class TrailSymbol(
    val section: TrailSymbolSection,
    val title: StringResource,
    val descriptionRes: StringResource,
    val iconRes: ImageResource,
) {
    BLUE_STRIPE(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_blue_title,
        descriptionRes = SharedRes.strings.trail_symbols_blue_description,
        iconRes = SharedRes.images.symbol_k,
    ),
    RED_STRIPE(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_red_title,
        descriptionRes = SharedRes.strings.trail_symbols_red_description,
        iconRes = SharedRes.images.symbol_p,
    ),
    YELLOW_STRIPE(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_yellow_title,
        descriptionRes = SharedRes.strings.trail_symbols_yellow_description,
        iconRes = SharedRes.images.symbol_s,
    ),
    GREEN_STRIPE(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_green_title,
        descriptionRes = SharedRes.strings.trail_symbols_green_description,
        iconRes = SharedRes.images.symbol_z,
    ),
    CROSS(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_cross_title,
        descriptionRes = SharedRes.strings.trail_symbols_cross_description,
        iconRes = SharedRes.images.symbol_kp,
    ),
    ROUND_TOUR(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_round_tour_title,
        descriptionRes = SharedRes.strings.trail_symbols_round_tour_description,
        iconRes = SharedRes.images.symbol_zc,
    ),
    EDUCATIONAL_TRAIL(
        section = TrailSymbolSection.ROUTE,
        title = SharedRes.strings.trail_symbols_educational_trail_title,
        descriptionRes = SharedRes.strings.trail_symbols_educational_trail_description,
        iconRes = SharedRes.images.symbol_st,
    ),
    TRIANGLE(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_triangle_title,
        descriptionRes = SharedRes.strings.trail_symbols_triangle_description,
        iconRes = SharedRes.images.symbol_z3,
    ),
    SQUARE(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_square_title,
        descriptionRes = SharedRes.strings.trail_symbols_square_description,
        iconRes = SharedRes.images.symbol_k4,
    ),
    CIRCLE(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_circle_title,
        descriptionRes = SharedRes.strings.trail_symbols_circle_description,
        iconRes = SharedRes.images.symbol_pq,
    ),
    CAVE(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_cave_title,
        descriptionRes = SharedRes.strings.trail_symbols_cave_description,
        iconRes = SharedRes.images.symbol_sb,
    ),
    RUIN(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_ruin_title,
        descriptionRes = SharedRes.strings.trail_symbols_ruin_description,
        iconRes = SharedRes.images.symbol_zl,
    ),
    CHAPEL(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_chapel_title,
        descriptionRes = SharedRes.strings.trail_symbols_chapel_description,
        iconRes = SharedRes.images.symbol_stmp,
    ),
    MONUMENT(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_monument_title,
        descriptionRes = SharedRes.strings.trail_symbols_monument_description,
        iconRes = SharedRes.images.symbol_peml,
    ),
    STAMP(
        section = TrailSymbolSection.TARGET,
        title = SharedRes.strings.trail_symbols_stamp_title,
        descriptionRes = SharedRes.strings.trail_symbols_stamp_description,
        iconRes = SharedRes.images.symbol_kpec,
    ),
}
