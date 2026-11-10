package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class SupportAnimal(
    val productId: String,
    val type: SupportType,
    val nameRes: StringResource,
    val iconRes: ImageResource,
    val colorRes: ColorResource,
    val containerColorRes: ColorResource,
    val isIconFacingRight: Boolean,
) {
    SQUIRREL(
        productId = "huki_support_one_time_level_2",
        type = SupportType.ONE_TIME,
        nameRes = SharedRes.strings.support_animal_squirrel,
        iconRes = SharedRes.images.ic_support_squirrel,
        colorRes = SharedRes.colors.supportSquirrel,
        containerColorRes = SharedRes.colors.supportSquirrelContainer,
        isIconFacingRight = true,
    ),
    OWL(
        productId = "huki_support_one_time_level_1",
        type = SupportType.ONE_TIME,
        nameRes = SharedRes.strings.support_animal_owl,
        iconRes = SharedRes.images.ic_support_owl,
        colorRes = SharedRes.colors.supportOwl,
        containerColorRes = SharedRes.colors.supportOwlContainer,
        isIconFacingRight = false,
    ),
    HEDGEHOG(
        productId = "huki_support_one_time_level_0",
        type = SupportType.ONE_TIME,
        nameRes = SharedRes.strings.support_animal_hedgehog,
        iconRes = SharedRes.images.ic_support_hedgehog,
        colorRes = SharedRes.colors.supportHedgehog,
        containerColorRes = SharedRes.colors.supportHedgehogContainer,
        isIconFacingRight = true,
    ),
    DEER(
        productId = "huki_support_recurring_level_2",
        type = SupportType.RECURRING,
        nameRes = SharedRes.strings.support_animal_deer,
        iconRes = SharedRes.images.ic_support_deer,
        colorRes = SharedRes.colors.supportDeer,
        containerColorRes = SharedRes.colors.supportDeerContainer,
        isIconFacingRight = true,
    ),
    BOAR(
        productId = "huki_support_recurring_level_1",
        type = SupportType.RECURRING,
        nameRes = SharedRes.strings.support_animal_boar,
        iconRes = SharedRes.images.ic_support_boar,
        colorRes = SharedRes.colors.supportBoar,
        containerColorRes = SharedRes.colors.supportBoarContainer,
        isIconFacingRight = false,
    ),
    BEAR(
        productId = "huki_support_recurring_level_0",
        type = SupportType.RECURRING,
        nameRes = SharedRes.strings.support_animal_bear,
        iconRes = SharedRes.images.ic_support_bear,
        colorRes = SharedRes.colors.supportBear,
        containerColorRes = SharedRes.colors.supportBearContainer,
        isIconFacingRight = true,
    ),
    ;

    /**
     * Level 0 is the highest tier, taken from the product ID shared with the legacy app.
     */
    val supportLevel: Int
        get() = productId.substringAfterLast("_level_").toInt()

    companion object {
        fun fromProductId(productId: String): SupportAnimal? = entries.firstOrNull { it.productId == productId }
    }
}
