import CoreGraphics
import SwiftUI

enum Dimens {
    static let sectionSpacing: CGFloat = 18

    static let infoWindowBorder: CGFloat = 1.5
    static let infoWindowCornerRadius: CGFloat = 16
    static let infoWindowTailWidth: CGFloat = 16
    static let infoWindowTailHeight: CGFloat = 8
    static let infoWindowMarkerPadding: CGFloat = 1

    static let zoomControlIconSize: CGFloat = 20

    static let gpxContentPadding = EdgeInsets(
        top: 120,
        leading: 60,
        bottom: 300,
        trailing: 60
    )

    static let gpxDetailsCollapsedDetentHeight: CGFloat = 240
    static let gpxDetailsExpandedDetentHeight: CGFloat = 320
    static let gpxDetailsExpandedWithEndDetentHeight: CGFloat = 350

    static let whatsNewDetentHeight: CGFloat = 350
    static let whatsNewDetentHeightWithMessage: CGFloat = 400
}
