package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import hu.mostoha.mobile.kmp.huki.data.Landscapes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionSortMode
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDistanceRange
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileDay
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.model.domain.GpxLandscape
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.maplibre.spatialk.units.extensions.inKilometers
import kotlin.time.Instant

/**
 * Groups the files into the sections of [sortMode]: newest first by day, longest first by distance, and by
 * landscape in the bundled order with the tracks outside of every landscape last.
 */
fun List<GpxFileItem>.toGpxFileSections(
    sortMode: GpxCollectionSortMode,
    now: Instant,
    timeZone: TimeZone,
): List<GpxFileSection> {
    val days = GpxDays(now, timeZone)
    val newestFirst = sortedByDescending { it.lastUsed }
    return when (sortMode) {
        GpxCollectionSortMode.DATE ->
            newestFirst
                .groupBy { days.of(it) }
                .map { (day, files) ->
                    GpxFileSection(
                        header = GpxFileHeader.Day(day),
                        items = files.map { GpxCollectionItem(file = it, landscape = it.landscape) },
                    )
                }
        GpxCollectionSortMode.DISTANCE -> sortedByDescending { it.totalDistance }
            .groupBy { GpxDistanceRange.of(it.totalDistance.inKilometers) }
            .entries
            .sortedBy { it.key }
            .map { (range, files) ->
                GpxFileSection(
                    header = GpxFileHeader.Distance(range),
                    items = files.map { GpxCollectionItem(file = it, landscape = it.landscape) },
                )
            }
        GpxCollectionSortMode.LANDSCAPE ->
            newestFirst
                .groupBy { it.landscape }
                .entries
                .sortedWith(
                    compareBy<Map.Entry<GpxLandscape?, List<GpxFileItem>>> { it.key == null }
                        .thenBy { entry -> Landscapes.indexOfFirst { it.id == entry.key?.id } },
                )
                .map { (landscape, files) ->
                    GpxFileSection(
                        header = landscape?.let { GpxFileHeader.Landscape(it) } ?: GpxFileHeader.OutsideLandscapes,
                        items = files.map { GpxCollectionItem(file = it, landscape = null) },
                    )
                }
    }
}

private val GpxFileItem.lastUsed: Instant
    get() = lastOpened ?: lastModified

private val GpxFileItem.landscape: GpxLandscape?
    get() = Landscapes
        .firstOrNull { it.id == landscapeId }
        ?.let {
            GpxLandscape(id = it.id, nameRes = it.nameRes, colorRes = it.colorRes, iconRes = it.landscapeType.iconRes)
        }

private class GpxDays(
    now: Instant,
    private val timeZone: TimeZone,
) {
    private val today = now.toLocalDateTime(timeZone).date
    private val yesterday = today.minus(DatePeriod(days = 1))

    fun of(file: GpxFileItem): GpxFileDay =
        when (val date = file.lastUsed.toLocalDateTime(timeZone).date) {
            today -> GpxFileDay.Today
            yesterday -> GpxFileDay.Yesterday
            else -> GpxFileDay.Date(date.formatAsLabel())
        }

    private fun LocalDate.formatAsLabel(): String = toString().replace('-', '.')
}
