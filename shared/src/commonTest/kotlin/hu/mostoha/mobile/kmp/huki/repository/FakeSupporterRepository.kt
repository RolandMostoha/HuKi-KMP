package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.ScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupportType
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSupporterRepository : SupporterRepository {
    override val supporterState = MutableStateFlow(SupporterState())
    var purchaseResult: BillingPurchaseResult = BillingPurchaseResult.Cancelled
    var purchaseGate: CompletableDeferred<Unit>? = null
    val purchasedAnimals = mutableListOf<SupportAnimal>()
    val chosenAnimals = mutableListOf<SupportAnimal>()
    var refreshCount = 0
        private set

    override suspend fun refresh() {
        refreshCount++
    }

    override suspend fun purchase(animal: SupportAnimal): BillingPurchaseResult {
        purchasedAnimals += animal
        purchaseGate?.await()
        (purchaseResult as? BillingPurchaseResult.Scheduled)?.let { result ->
            supporterState.update { it.copy(scheduledSubscription = ScheduledSubscription(animal, result.startTime)) }
        }
        if (purchaseResult is BillingPurchaseResult.Success) {
            supporterState.update { state ->
                val purchaseCount = (state.oneTimePurchaseCounts[animal] ?: 0) + 1
                when (animal.type) {
                    SupportType.ONE_TIME -> state.copy(oneTimePurchaseCounts = state.oneTimePurchaseCounts + (animal to purchaseCount))
                    SupportType.RECURRING -> state.copy(activeSubscriptions = state.activeSubscriptions + animal)
                }.copy(chosenAnimal = animal)
            }
        }
        return purchaseResult
    }

    override suspend fun chooseAnimal(animal: SupportAnimal) {
        chosenAnimals += animal
        supporterState.update { it.copy(chosenAnimal = animal) }
    }
}
