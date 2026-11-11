package hu.mostoha.mobile.kmp.huki.util

import co.touchlab.kermit.Logger
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.HttpRequest
import com.mapbox.common.HttpRequestOrResponse
import com.mapbox.common.HttpResponse
import com.mapbox.common.HttpResponseData
import com.mapbox.common.HttpServiceInterceptorInterface
import com.mapbox.common.HttpServiceInterceptorRequestContinuation
import com.mapbox.common.HttpServiceInterceptorResponseContinuation

/**
 * Sees every Mapbox HTTP request: each one must reach its continuation, or map loading stalls.
 */
class OverlayTileCacheInterceptor : HttpServiceInterceptorInterface {
    override fun onRequest(request: HttpRequest, continuation: HttpServiceInterceptorRequestContinuation) {
        continuation.run(HttpRequestOrResponse(request))
    }

    override fun onResponse(response: HttpResponse, continuation: HttpServiceInterceptorResponseContinuation) {
        val result = runCatching { response.withOverlayCacheControl() }
            .onFailure { Logger.e(it) { "Map: Failed to apply overlay tile cache headers." } }
            .getOrDefault(response)
        continuation.run(result)
    }

    private fun HttpResponse.withOverlayCacheControl(): HttpResponse {
        val data = result.value ?: return this
        val headers = OverlayTileCachePolicy.headersWithCacheControl(request.url, data.code, data.headers)
            ?: return this
        return HttpResponse(
            requestId,
            request,
            ExpectedFactory.createValue(HttpResponseData(HashMap(headers), data.code, data.data)),
        )
    }
}
