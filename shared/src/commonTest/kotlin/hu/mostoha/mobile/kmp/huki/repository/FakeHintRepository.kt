package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Hint
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHintRepository(initial: Set<Hint> = emptySet()) : HintRepository {
    override val dismissedHints = MutableStateFlow(initial)

    override suspend fun dismissHint(hint: Hint) {
        dismissedHints.value += hint
    }
}
