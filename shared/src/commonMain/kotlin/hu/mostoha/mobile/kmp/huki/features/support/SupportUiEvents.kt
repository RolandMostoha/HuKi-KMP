package hu.mostoha.mobile.kmp.huki.features.support

import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportSection

sealed interface SupportUiEvents {
    data object ScreenViewed : SupportUiEvents

    data class SectionOpened(val section: SupportSection) : SupportUiEvents

    data object RetryClicked : SupportUiEvents

    data class TierClicked(val animal: SupportAnimal) : SupportUiEvents

    data object PurchaseMessageDismissed : SupportUiEvents

    data object AlreadyPurchasedDialogDismissed : SupportUiEvents

    data object ScheduledDialogDismissed : SupportUiEvents

    data class SupporterAnimalChosen(val animal: SupportAnimal) : SupportUiEvents

    data object ManageSubscriptionsClicked : SupportUiEvents
}
