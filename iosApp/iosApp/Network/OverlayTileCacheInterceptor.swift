import MapboxCommon
import Shared

/// Sees every Mapbox HTTP request: each one must reach its continuation, or map loading stalls.
final class OverlayTileCacheInterceptor: HttpServiceInterceptorInterface {
    func onRequest(
        for request: HttpRequest,
        continuation: @escaping HttpServiceInterceptorRequestContinuation
    ) {
        continuation(HttpRequestOrResponse.fromHttpRequest(request))
    }

    func onResponse(
        for response: HttpResponse,
        continuation: @escaping HttpServiceInterceptorResponseContinuation
    ) {
        continuation(withOverlayCacheControl(response))
    }

    private func withOverlayCacheControl(_ response: HttpResponse) -> HttpResponse {
        guard case .success(let data) = response.result,
              let headers = OverlayTileCachePolicy.shared.headersWithCacheControl(
                url: response.request.url,
                code: data.code,
                headers: data.headers
              )
        else {
            return response
        }
        return HttpResponse(
            identifier: response.requestId,
            request: response.request,
            result: .success(HttpResponseData(headers: headers, code: data.code, data: data.data))
        )
    }
}
