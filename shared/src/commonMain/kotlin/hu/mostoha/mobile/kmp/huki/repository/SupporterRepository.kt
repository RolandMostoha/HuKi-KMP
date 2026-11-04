package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import kotlinx.coroutines.flow.StateFlow

interface SupporterRepository {
    val supporterState: StateFlow<SupporterState>

    /**
     * Syncs with the store, keeping the last known state when the store is unreachable.
     */
    suspend fun refresh()

    /**
     * A successful purchase also becomes the chosen animal, a scheduled subscription only once it starts.
     */
    suspend fun purchase(animal: SupportAnimal): BillingPurchaseResult

    suspend fun chooseAnimal(animal: SupportAnimal)
}
