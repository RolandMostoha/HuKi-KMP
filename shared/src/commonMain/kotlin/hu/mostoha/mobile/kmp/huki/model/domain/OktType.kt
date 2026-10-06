package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.data.AKT_FULL_TRAIL_ID
import hu.mostoha.mobile.kmp.huki.data.AKT_SECTIONS
import hu.mostoha.mobile.kmp.huki.data.OKT_FULL_TRAIL_ID
import hu.mostoha.mobile.kmp.huki.data.OKT_SECTIONS
import hu.mostoha.mobile.kmp.huki.data.RPDDK_FULL_TRAIL_ID
import hu.mostoha.mobile.kmp.huki.data.RPDDK_SECTIONS

enum class OktType(
    val title: StringResource,
    val subtitle: StringResource,
    val longSubtitle: StringResource,
    val icon: ImageResource,
    val sections: List<OktSection>,
    val fullTrailId: String,
    val stampPrefix: String,
    val gpxFile: FileResource,
    val websiteUrl: String,
    val sectionUrlTemplate: String,
) {
    OKT(
        title = SharedRes.strings.okt_okt_title,
        subtitle = SharedRes.strings.okt_okt_subtitle,
        longSubtitle = SharedRes.strings.okt_okt_subtitle,
        icon = SharedRes.images.ic_okt_okt,
        sections = OKT_SECTIONS,
        fullTrailId = OKT_FULL_TRAIL_ID,
        stampPrefix = "OKTPH",
        gpxFile = SharedRes.files.okt_teljes_bh_20260924_gpx,
        websiteUrl = "https://www.kektura.hu/okt-szakaszok",
        sectionUrlTemplate = "https://www.kektura.hu/okt-szakasz/%s",
    ),
    RPDDK(
        title = SharedRes.strings.okt_rpddk_title,
        subtitle = SharedRes.strings.okt_rpddk_subtitle,
        longSubtitle = SharedRes.strings.okt_rpddk_subtitle_long,
        icon = SharedRes.images.ic_okt_rpddk,
        sections = RPDDK_SECTIONS,
        fullTrailId = RPDDK_FULL_TRAIL_ID,
        stampPrefix = "DDKPH",
        gpxFile = SharedRes.files.rpddk_teljes_bh_20260806_gpx,
        websiteUrl = "https://www.kektura.hu/rpddk-szakaszok",
        sectionUrlTemplate = "https://www.kektura.hu/rpddk-szakasz/%s",
    ),
    AKT(
        title = SharedRes.strings.okt_akt_title,
        subtitle = SharedRes.strings.okt_akt_subtitle,
        longSubtitle = SharedRes.strings.okt_akt_subtitle,
        icon = SharedRes.images.ic_okt_akt,
        sections = AKT_SECTIONS,
        fullTrailId = AKT_FULL_TRAIL_ID,
        stampPrefix = "AKPH",
        gpxFile = SharedRes.files.ak_teljes_bh_20260924_gpx,
        websiteUrl = "https://www.kektura.hu/ak-szakaszok",
        sectionUrlTemplate = "https://www.kektura.hu/ak-szakasz/%s",
    ),
    ;

    companion object {
        const val KEKTURA_URL = "https://www.kektura.hu/"
    }
}
