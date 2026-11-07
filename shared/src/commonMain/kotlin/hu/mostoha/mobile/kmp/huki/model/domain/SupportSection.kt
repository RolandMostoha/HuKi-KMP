package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class SupportSection(
    val title: StringResource,
    val message: StringResource,
) {
    SPENDING(
        title = SharedRes.strings.support_reason_spending_title,
        message = SharedRes.strings.support_reason_spending_message,
    ),
    WHY(
        title = SharedRes.strings.support_reason_why_title,
        message = SharedRes.strings.support_reason_why_message,
    ),
    REWARDS(
        title = SharedRes.strings.support_reason_rewards_title,
        message = SharedRes.strings.support_reason_rewards_message,
    ),
}
