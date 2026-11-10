package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportRestoreResult
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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

    /**
     * Syncs the purchases from the store's servers first, unlike [refresh], which reads the store's cache.
     */
    suspend fun restore(): SupportRestoreResult

    suspend fun chooseAnimal(animal: SupportAnimal)
}

val SupporterRepository.supporterAnimal: Flow<SupportAnimal?>
    get() = supporterState.map { it.supporterAnimal }.distinctUntilChanged()
