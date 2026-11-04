package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink

class FakeLocationLinkResolver(
    var result: LocationLinkResult = LocationLinkResult.Unsupported,
) : LocationLinkResolver {
    val resolvedLinks = mutableListOf<IncomingLink>()

    override suspend fun resolve(link: IncomingLink): LocationLinkResult {
        resolvedLinks += link
        return result
    }
}
