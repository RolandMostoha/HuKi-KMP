package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Hint
import kotlinx.coroutines.flow.Flow

interface HintRepository {
    val dismissedHints: Flow<Set<Hint>>

    suspend fun dismissHint(hint: Hint)
}
