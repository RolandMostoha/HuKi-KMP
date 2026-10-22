package hu.mostoha.mobile.kmp.huki.data

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.TermeszetjaroTag

/**
 * Main landscapes of Hungary, focusing on the hiking related areas.
 *
 * Outlines are generated from OpenStreetMap, keyed by [Landscape.id]; the OSM source of each landscape is
 * listed in tools/python/generate_landscape_outlines.py.
 */
val Landscapes = listOf(
    Landscape(
        id = "bukk",
        nameRes = SharedRes.strings.landscape_bukk,
        colorRes = SharedRes.colors.landscapeBukk,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(48.0356833, 20.5239573),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "bukk",
            HikeRecommendation.AKTIVKALANDOR to "bukk",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359977",
            areaName = "Bükkvidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_BUKK,
    ),
    Landscape(
        id = "balaton_felvidek",
        nameRes = SharedRes.strings.landscape_balaton_felvidek,
        colorRes = SharedRes.colors.landscapeBalatonFelvidek,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(46.9174441, 17.7261084),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "balaton-felvidek",
            HikeRecommendation.AKTIVKALANDOR to "balaton-felvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598301",
            areaName = "Balaton-felvidék (Hegycsoport)",
        ),
        destinations = DESTINATIONS_BALATON,
    ),
    Landscape(
        id = "aggteleki_karszt",
        nameRes = SharedRes.strings.landscape_aggteleki_karszt,
        colorRes = SharedRes.colors.landscapeAggtelekiKarszt,
        landscapeType = LandscapeType.CAVE_SYSTEM,
        center = Location(48.4542508, 20.6350029),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "aggteleki-karszt",
            HikeRecommendation.AKTIVKALANDOR to "aggtelek-es-cserehat",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258172",
            areaName = "Aggteleki Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_AGGTELEK,
    ),
    Landscape(
        id = "mecsek",
        nameRes = SharedRes.strings.landscape_mecsek,
        colorRes = SharedRes.colors.landscapeMecsek,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(46.1375511, 18.2469531),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "pecs-baranya",
            HikeRecommendation.AKTIVKALANDOR to "mecsek-villany",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23360001",
            areaName = "Mecsek vidéke (Dél-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_MECSEK,
    ),
    Landscape(
        id = "matra",
        nameRes = SharedRes.strings.landscape_matra,
        colorRes = SharedRes.colors.landscapeMatra,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(47.8702858, 19.9453253),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "matra",
            HikeRecommendation.AKTIVKALANDOR to "matra",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359984",
            areaName = "Mátravidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_MATRA,
    ),
    Landscape(
        id = "borzsony",
        nameRes = SharedRes.strings.landscape_borzsony,
        colorRes = SharedRes.colors.landscapeBorzsony,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(47.9128315, 18.9494417),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "dunakanyar",
            HikeRecommendation.AKTIVKALANDOR to "borzsony-es-dunakanyar",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359982",
            areaName = "Börzsönyvidék (Közép-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_BORZSONY,
    ),
    Landscape(
        id = "pilis_hegyseg",
        nameRes = SharedRes.strings.landscape_pilis_hegyseg,
        colorRes = SharedRes.colors.landscapePilisHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6427423, 18.8986191),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "budapest-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "pilis-es-visegradi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598246",
            areaName = "Pilis-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_PILIS,
    ),
    Landscape(
        id = "visegradi_hegyseg",
        nameRes = SharedRes.strings.landscape_visegradi_hegyseg,
        colorRes = SharedRes.colors.landscapeVisegradiHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_CASTLE,
        center = Location(47.7210692, 18.9181598),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "dunakanyar",
            HikeRecommendation.AKTIVKALANDOR to "pilis-es-visegradi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359987",
            areaName = "Visegrádi-hegység (Közép-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_VISGERAD,
    ),
    Landscape(
        id = "bakony",
        nameRes = SharedRes.strings.landscape_bakony,
        colorRes = SharedRes.colors.landscapeBakony,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.1624906, 17.7835194),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "bakony-veszprem",
            HikeRecommendation.AKTIVKALANDOR to "bakony",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359991",
            areaName = "Bakonyvidék (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_BAKONY,
    ),
    Landscape(
        id = "gerecse_hegyseg",
        nameRes = SharedRes.strings.landscape_gerecse_hegyseg,
        colorRes = SharedRes.colors.landscapeGerecseHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6177834, 18.5489089),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "vertes-gerecse",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598247",
            areaName = "Gerecse (Hegycsoport)",
        ),
        destinations = DESTINATIONS_GERECSE,
    ),
    Landscape(
        id = "keszthelyi_hegyseg",
        nameRes = SharedRes.strings.landscape_keszthelyi_hegyseg,
        colorRes = SharedRes.colors.landscapeKeszthelyiHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(46.8503130, 17.2709995),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "keszthely-es-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "kis-balaton",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598291",
            areaName = "Keszthelyi-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_KESZTHELY,
    ),
    Landscape(
        id = "cserhat",
        nameRes = SharedRes.strings.landscape_cserhat,
        colorRes = SharedRes.colors.landscapeCserhat,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.8688179, 19.4148133),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "palocfold",
            HikeRecommendation.AKTIVKALANDOR to "cserhat",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359979",
            areaName = "Cserhátvidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_CSERHAT,
    ),
    Landscape(
        id = "karancs_medves",
        nameRes = SharedRes.strings.landscape_karancs_medves,
        colorRes = SharedRes.colors.landscapeKarancsMedves,
        landscapeType = LandscapeType.MOUNTAIN_WITH_CASTLE,
        center = Location(48.1336, 19.7356),
        areaTags = emptyMap(),
        termeszetjaroTag = null,
        destinations = DESTINATIONS_KARANCS_MEDVES,
    ),
    Landscape(
        id = "zempleni_hegyseg",
        nameRes = SharedRes.strings.landscape_zempleni_hegyseg,
        colorRes = SharedRes.colors.landscapeZempleniHegyseg,
        landscapeType = LandscapeType.WINE_AREA,
        center = Location(48.3440651, 21.4169262),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "zemplen",
            HikeRecommendation.AKTIVKALANDOR to "zemplen",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014010",
            areaName = "Zempléni Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_ZEMPLEN,
    ),
    Landscape(
        id = "vertes",
        nameRes = SharedRes.strings.landscape_vertes,
        colorRes = SharedRes.colors.landscapeVertes,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        center = Location(47.4175701, 18.3625658),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "vertes-gerecse",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359992",
            areaName = "Vértes és vidéke (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_VERTES,
    ),
    Landscape(
        id = "velencei_hegyseg",
        nameRes = SharedRes.strings.landscape_velencei_hegyseg,
        colorRes = SharedRes.colors.landscapeVelenceiHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(47.2654220, 18.5854126),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "velencei-to-es-volgyvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "59627042",
            areaName = "Velencei-hegység (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_VELENCE,
    ),
    Landscape(
        id = "somogyi_dombsag",
        nameRes = SharedRes.strings.landscape_somogyi_dombsag,
        colorRes = SharedRes.colors.landscapeSomogyiDombsag,
        landscapeType = LandscapeType.STAR_GAZING_AREA,
        center = Location(46.5277, 17.8860),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "tolna",
            HikeRecommendation.AKTIVKALANDOR to "zselic",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014012",
            areaName = "Zselici Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_SOMOGYI_DOMBSAG,
    ),
    Landscape(
        id = "villanyi_hegyseg",
        nameRes = SharedRes.strings.landscape_villanyi_hegyseg,
        colorRes = SharedRes.colors.landscapeVillanyiHegyseg,
        landscapeType = LandscapeType.WINE_AREA,
        center = Location(45.8747512, 18.2730710),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "pecs-baranya",
            HikeRecommendation.AKTIVKALANDOR to "mecsek-villany",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598286",
            areaName = "Villányi-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_VILLANY,
    ),
    Landscape(
        id = "koszegi_hegyseg",
        nameRes = SharedRes.strings.landscape_koszegi_hegyseg,
        colorRes = SharedRes.colors.landscapeKoszegiHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.3307697, 16.4054968),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "koszeg-es-szombathely-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "koszegi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25013914",
            areaName = "Kőszegi Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_KOSZEG,
    ),
    Landscape(
        id = "heves_borsodi_dombsag",
        nameRes = SharedRes.strings.landscape_heves_borsodi_dombsag,
        colorRes = SharedRes.colors.landscapeHevesBorsodiDombsag,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        center = Location(48.1120709, 20.1976971),
        areaTags = emptyMap(),
        termeszetjaroTag = null,
        destinations = DESTINATIONS_HEVES,
    ),
    Landscape(
        id = "budai_hegyseg",
        nameRes = SharedRes.strings.landscape_budai_hegyseg,
        colorRes = SharedRes.colors.landscapeBudaiHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.5428510, 18.9236294),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "budapest",
            HikeRecommendation.AKTIVKALANDOR to "fovaros-es-kornyeke/budapest-es-agglomeracio/",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598254",
            areaName = "Budai-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_BUDAPEST,
    ),
    Landscape(
        id = "soproni_hegyseg",
        nameRes = SharedRes.strings.landscape_soproni_hegyseg,
        colorRes = SharedRes.colors.landscapeSoproniHegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6438256, 16.4890472),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "sopron-es-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "soproni-hegyseg-es-ferto-to",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598250",
            areaName = "Soproni-hegység (Hegyvonulat)",
        ),
        destinations = DESTINATIONS_SOPRON,
    ),
    Landscape(
        id = "ferto_to_videke",
        nameRes = SharedRes.strings.landscape_ferto_to_videke,
        colorRes = SharedRes.colors.landscapeFertoToVideke,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(47.6765, 16.7420),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "sopron-es-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "soproni-hegyseg-es-ferto-to",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258163",
            areaName = "Fertő–Hanság Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_FERTO,
    ),
    Landscape(
        id = "hortobagy",
        nameRes = SharedRes.strings.landscape_hortobagy,
        colorRes = SharedRes.colors.landscapeHortobagy,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(47.49350, 21.05344),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "hortobagy-tisza-to-debrecen",
            HikeRecommendation.AKTIVKALANDOR to "tisza-to-es-hortobagy",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258164",
            areaName = "Hortobágyi Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_HORTOBAGY,
    ),
    Landscape(
        id = "orseg",
        nameRes = SharedRes.strings.landscape_orseg,
        colorRes = SharedRes.colors.landscapeOrseg,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(46.83921, 16.40093),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "orseg",
            HikeRecommendation.AKTIVKALANDOR to "orseg-es-vendvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258166",
            areaName = "Őrségi Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_ORSEG,
    ),
    Landscape(
        id = "alfold",
        nameRes = SharedRes.strings.landscape_alfold,
        colorRes = SharedRes.colors.landscapeAlfold,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(latitude = 46.55921, longitude = 20.19168),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "del-alfold",
            HikeRecommendation.AKTIVKALANDOR to "del-alfold",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23406038",
            areaName = "Dél-Alföld (Magyarország)",
        ),
        destinations = DESTINATIONS_ALFOLD,
    ),
    Landscape(
        id = "zalai_dombsag",
        nameRes = SharedRes.strings.landscape_zalai_dombsag,
        colorRes = SharedRes.colors.landscapeZalaiDombsag,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(latitude = 46.7060, longitude = 16.7191),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "gocsej-zala",
            HikeRecommendation.AKTIVKALANDOR to "zalai-dombsag",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359999",
            areaName = "Zalai-dombság (Nyugat-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_ZALA,
    ),
    Landscape(
        id = "kis_balaton",
        nameRes = SharedRes.strings.landscape_kis_balaton,
        colorRes = SharedRes.colors.landscapeKisBalaton,
        landscapeType = LandscapeType.LAKE,
        center = Location(latitude = 46.6400, longitude = 17.2000),
        areaTags = mapOf(
            HikeRecommendation.AKTIVKALANDOR to "kis-balaton",
        ),
        destinations = DESTINATIONS_KIS_BALATON,
    ),
    Landscape(
        id = "szigetkoz",
        nameRes = SharedRes.strings.landscape_szigetkoz,
        colorRes = SharedRes.colors.landscapeSzigetkoz,
        landscapeType = LandscapeType.LAKE,
        center = Location(latitude = 47.8615, longitude = 17.4010),
        areaTags = mapOf(
            HikeRecommendation.AKTIVKALANDOR to "szigetkoz-es-pannonhalma",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "22729870",
            areaName = "Szigetközi Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_SZIGETKOZ,
    ),
    Landscape(
        id = "gemenc",
        nameRes = SharedRes.strings.landscape_gemenc,
        colorRes = SharedRes.colors.landscapeGemenc,
        landscapeType = LandscapeType.FOREST_AREA,
        center = Location(46.2, 18.84),
        areaTags = emptyMap(),
        destinations = DESTINATIONS_GEMENC,
    ),
)

val LandscapeByDestinationId: Map<String, Landscape> =
    Landscapes.flatMap { landscape -> landscape.destinations.map { it.id to landscape } }.toMap()
