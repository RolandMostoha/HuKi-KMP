package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_TRAIL
import hu.mostoha.mobile.kmp.huki.model.domain.OktTrail
import hu.mostoha.mobile.kmp.huki.model.domain.OktType

class FakeOktRepository(
    private val trail: OktTrail = TEST_OKT_TRAIL,
    private val exception: Exception? = null,
) : OktRepository {
    override suspend fun getOktTrail(type: OktType): OktTrail {
        exception?.let { throw it }
        return trail
    }
}
