import SwiftUI

enum AdaptiveLayout {
    // Max content width for full-screen scrollable screens on wide devices (iPad / landscape).
    static let readableContentWidth: CGFloat = 640
    // Max width for the floating search bar on wide layouts (landscape / regular width).
    static let searchBarMaxWidth: CGFloat = 350
}

struct LayoutMode {
    let horizontalSizeClass: UserInterfaceSizeClass?
    let verticalSizeClass: UserInterfaceSizeClass?

    var isLandscape: Bool {
        verticalSizeClass == .compact
    }

    var isPad: Bool {
        horizontalSizeClass == .regular && verticalSizeClass == .regular
    }

    var isWide: Bool {
        isLandscape || isPad
    }
}

extension EnvironmentValues {
    var layoutMode: LayoutMode {
        LayoutMode(horizontalSizeClass: horizontalSizeClass, verticalSizeClass: verticalSizeClass)
    }
}

extension AdaptiveLayout {
    // True when the map viewport is wider than tall (any device in landscape, iPad included) —
    static func isLandscapeViewport(mapSize: CGSize) -> Bool {
        mapSize.width > mapSize.height
    }
}

extension View {
    // Caps content width and centers it on wide screens; unchanged on portrait phones.
    func readableWidth(_ max: CGFloat = AdaptiveLayout.readableContentWidth) -> some View {
        frame(maxWidth: max)
            .frame(maxWidth: .infinity, alignment: .center)
    }
}
