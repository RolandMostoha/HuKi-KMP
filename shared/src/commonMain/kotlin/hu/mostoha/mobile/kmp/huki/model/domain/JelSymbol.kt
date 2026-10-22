package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes

/**
 * Hungarian trail markings, keyed by the OSM `jel` tag value.
 */
enum class JelSymbol(
    val osmValue: String,
    val icon: ImageResource,
) {
    K("k", SharedRes.images.symbol_k),
    KP("k+", SharedRes.images.symbol_kp),
    K3("k3", SharedRes.images.symbol_k3),
    K4("k4", SharedRes.images.symbol_k4),
    KQ("kq", SharedRes.images.symbol_kq),
    KB("kb", SharedRes.images.symbol_kb),
    KL("kl", SharedRes.images.symbol_kl),
    KC("kc", SharedRes.images.symbol_kc),
    KT("kt", SharedRes.images.symbol_kt),
    KEML("keml", SharedRes.images.symbol_keml),
    KTMP("ktmp", SharedRes.images.symbol_ktmp),
    KX("kx", SharedRes.images.symbol_kx),
    KII("kii", SharedRes.images.symbol_kii),
    KPEC("kpec", SharedRes.images.symbol_kpec),
    P("p", SharedRes.images.symbol_p),
    PP("p+", SharedRes.images.symbol_pp),
    P3("p3", SharedRes.images.symbol_p3),
    P4("p4", SharedRes.images.symbol_p4),
    PQ("pq", SharedRes.images.symbol_pq),
    PB("pb", SharedRes.images.symbol_pb),
    PL("pl", SharedRes.images.symbol_pl),
    PC("pc", SharedRes.images.symbol_pc),
    PT("pt", SharedRes.images.symbol_pt),
    PEML("peml", SharedRes.images.symbol_peml),
    PTMP("ptmp", SharedRes.images.symbol_ptmp),
    PX("px", SharedRes.images.symbol_px),
    PII("pii", SharedRes.images.symbol_pii),
    Z("z", SharedRes.images.symbol_z),
    ZP("z+", SharedRes.images.symbol_zp),
    Z3("z3", SharedRes.images.symbol_z3),
    Z4("z4", SharedRes.images.symbol_z4),
    ZQ("zq", SharedRes.images.symbol_zq),
    ZB("zb", SharedRes.images.symbol_zb),
    ZL("zl", SharedRes.images.symbol_zl),
    ZC("zc", SharedRes.images.symbol_zc),
    ZT("zt", SharedRes.images.symbol_zt),
    ZEML("zeml", SharedRes.images.symbol_zeml),
    ZTMP("ztmp", SharedRes.images.symbol_ztmp),
    ZX("zx", SharedRes.images.symbol_zx),
    ZII("zii", SharedRes.images.symbol_zii),
    S("s", SharedRes.images.symbol_s),
    SP("s+", SharedRes.images.symbol_sp),
    S3("s3", SharedRes.images.symbol_s3),
    S4("s4", SharedRes.images.symbol_s4),
    SQ("sq", SharedRes.images.symbol_sq),
    SB("sb", SharedRes.images.symbol_sb),
    SL("sl", SharedRes.images.symbol_sl),
    SC("sc", SharedRes.images.symbol_sc),
    ST("st", SharedRes.images.symbol_st),
    SEML("seml", SharedRes.images.symbol_seml),
    STMP("stmp", SharedRes.images.symbol_stmp),
    SX("sx", SharedRes.images.symbol_sx),
    SII("sii", SharedRes.images.symbol_sii),
    T("t", SharedRes.images.symbol_t),
    LM("lm", SharedRes.images.symbol_lm),
    KM("km", SharedRes.images.symbol_km),
    SM("sm", SharedRes.images.symbol_sm),
    ZM("zm", SharedRes.images.symbol_zm),
    PM("pm", SharedRes.images.symbol_pm),
    SMZ("smz", SharedRes.images.symbol_smz),
    ;

    companion object {
        fun fromOsmValue(osmValue: String): JelSymbol? = entries.firstOrNull { it.osmValue == osmValue }
    }
}
