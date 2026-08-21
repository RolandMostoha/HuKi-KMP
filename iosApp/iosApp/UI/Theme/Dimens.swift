import CoreGraphics
import SwiftUI

enum Dimens {
    static let sectionSpacing: CGFloat = 18

    static let infoWindowBorder: CGFloat = 1.5
    static let infoWindowCornerRadius: CGFloat = 16
    static let infoWindowTailWidth: CGFloat = 16
    static let infoWindowTailHeight: CGFloat = 8
    static let infoWindowMarkerPadding: CGFloat = 1
    static let infoWindowHorizontalPadding: CGFloat = 10
    static let infoWindowVerticalPadding: CGFloat = 6
    static let infoWindowDistanceFontSize: CGFloat = 13
    static let infoWindowTravelTimeFontSize: CGFloat = 11

    static let zoomControlIconSize: CGFloat = 20

    static let gpxContentPaddingPortrait = EdgeInsets(
        top: 120,
        leading: 60,
        bottom: 300,
        trailing: 60
    )
    static let gpxContentPaddingLandscape = EdgeInsets(
        top: 60,
        leading: 60,
        bottom: 60,
        trailing: 60
    )

    static let placeDetailsContentPaddingPortrait = EdgeInsets(
        top: 120,
        leading: 60,
        bottom: 260,
        trailing: 60
    )
    static let placeDetailsContentPaddingLandscape = EdgeInsets(
        top: 60,
        leading: 60,
        bottom: 60,
        trailing: 60
    )

    static func routePlannerContentPaddingPortrait(sheetHeight: CGFloat) -> EdgeInsets {
        EdgeInsets(
            top: 100,
            leading: 60,
            bottom: sheetHeight + 20,
            trailing: 60
        )
    }
    static let routePlannerContentPaddingLandscape = EdgeInsets(
        top: 60,
        leading: 60,
        bottom: 60,
        trailing: 60
    )

    static let gpxDetailsCollapsedDetentHeight: CGFloat = 240
    static let gpxDetailsExpandedDetentHeight: CGFloat = 382
    static let gpxDetailsExpandedWithEndDetentHeight: CGFloat = 412
    static let gpxDetailsPadExpandedDetentHeight: CGFloat = 382
    static let gpxDetailsPadExpandedWithEndDetentHeight: CGFloat = 442

    static func gpxDetailsExpandedHeight(hasEnd: Bool, isPad: Bool) -> CGFloat {
        if isPad {
            return hasEnd ? gpxDetailsPadExpandedWithEndDetentHeight : gpxDetailsPadExpandedDetentHeight
        }
        return hasEnd ? gpxDetailsExpandedWithEndDetentHeight : gpxDetailsExpandedDetentHeight
    }

    static let placeDetailsDetentHeight: CGFloat = 215

    static let routePlannerDetentHeight: CGFloat = 520
    static let routePlannerMinimizedDetentHeight: CGFloat = 190

    static let whatsNewDetentHeight: CGFloat = 350
    static let whatsNewDetentHeightWithMessage: CGFloat = 400
}
