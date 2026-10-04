package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.OktTrail
import hu.mostoha.mobile.kmp.huki.model.domain.OktType

interface OktRepository {
    suspend fun getOktTrail(type: OktType): OktTrail
}
