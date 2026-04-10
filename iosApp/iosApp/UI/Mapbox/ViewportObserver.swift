import MapboxMaps
import Shared
import SwiftUI

final class ViewportObserver: ViewportStatusObserver {
    var onFollowingDisabled: (() -> Void)?

    func viewportStatusDidChange(
        from fromStatus: ViewportStatus,
        to toStatus: ViewportStatus,
        reason: ViewportStatusChangeReason
    ) {
        if fromStatus.isFollow && (toStatus.isIdle || toStatus.isOverview) {
            onFollowingDisabled?()
        }
    }
}

private extension ViewportStatus {
    var isIdle: Bool {
        if case .idle = self { return true }
        return false
    }

    var isFollow: Bool {
        if case .state(let state) = self { return state is FollowPuckViewportState }
        return false
    }

    var isOverview: Bool {
        switch self {
        case .state(let state):
            return state is OverviewViewportState
        case .transition(_, let toState):
            return toState is OverviewViewportState
        default:
            return false
        }
    }
}
