import MapboxMaps
import Shared
import SwiftUI

final class ViewportObserver: ViewportStatusObserver {
    var onFollowToIdle: (() -> Void)?

    func viewportStatusDidChange(
        from fromStatus: ViewportStatus,
        to toStatus: ViewportStatus,
        reason: ViewportStatusChangeReason
    ) {
        switch (fromStatus, toStatus) {
        case let (.state(state), .idle) where state is FollowPuckViewportState:
            onFollowToIdle?()
        default:
            break
        }
    }
}
