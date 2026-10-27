import CoreGraphics
import Shared
import SwiftUI

enum Dimens {
    static let sectionSpacing: CGFloat = 18

    static let discoverFabSize: CGFloat = 52

    static let infoWindowCornerRadius: CGFloat = 16
    static let infoWindowTailWidth: CGFloat = 16
    static let infoWindowTailHeight: CGFloat = 8
    static let infoWindowMarkerPadding: CGFloat = 1
    static let infoWindowHorizontalPadding: CGFloat = 10
    static let infoWindowVerticalPadding: CGFloat = 6
    static let infoWindowLineSpacing: CGFloat = 2
    static let infoWindowNameFontSize: CGFloat = 13
    static let infoWindowNameMaxWidth: CGFloat = 160
    static let infoWindowDetailsFontSize: CGFloat = 13

    static let zoomControlIconSize: CGFloat = 20

    static let gpxContentPaddingPortrait = EdgeInsets(
        top: 120,
        leading: 60,
        bottom: 362,
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

    static func oktContentPaddingPortrait(sheetHeight: CGFloat) -> EdgeInsets {
        EdgeInsets(
            top: 90,
            leading: 40,
            bottom: sheetHeight + 44,
            trailing: 40
        )
    }
    static let oktStartedContentPaddingPortrait = EdgeInsets(
        top: 120,
        leading: 40,
        bottom: 140,
        trailing: 40
    )
    static let oktContentPaddingLandscape = EdgeInsets(
        top: 40,
        leading: 40,
        bottom: 40,
        trailing: 40
    )

    static func landscapesContentPaddingPortrait(sheetHeight: CGFloat) -> EdgeInsets {
        EdgeInsets(
            top: 134,
            leading: 42,
            bottom: sheetHeight + 24,
            trailing: 42
        )
    }
    static func landscapesDestinationContentPaddingPortrait(sheetHeight: CGFloat) -> EdgeInsets {
        EdgeInsets(
            top: 300,
            leading: 40,
            bottom: sheetHeight + 44,
            trailing: 40
        )
    }
    static let landscapesContentPaddingLandscape = EdgeInsets(
        top: 40,
        leading: 40,
        bottom: 40,
        trailing: 40
    )

    static let oktDetentMinHeight = CGFloat(SharedDimens.shared.OKT_SHEET_MIN_HEIGHT)

    static func oktDetentHeight(screenHeight: CGFloat) -> CGFloat {
        max(screenHeight * SharedDimens.shared.OKT_SHEET_FRACTION, oktDetentMinHeight)
    }
    static func landscapesDetentHeight(screenHeight: CGFloat) -> CGFloat {
        max(
            screenHeight * SharedDimens.shared.LANDSCAPES_SHEET_FRACTION,
            SharedDimens.shared.LANDSCAPES_SHEET_MIN_HEIGHT
        )
    }
    static let oktBadgeSize: CGFloat = 48
    static let oktStampCardWidth: CGFloat = 148
    static let oktStampCardHeight: CGFloat = 112
    static let oktStampIconSize: CGFloat = 30
    static let oktMarkerSize: CGFloat = 30
    static let oktSelectedMarkerSize: CGFloat = 40
    static let oktEdgeMarkerSize: CGFloat = 36
    static let oktInfoWindowWidth: CGFloat = 290
    static let oktStopFabSize: CGFloat = 56
    static let destinationMarkerSize: CGFloat = 30
    static let destinationSelectedMarkerSize: CGFloat = 40
    static let destinationInfoWindowWidth: CGFloat = 290
    static let landscapeIconSize: CGFloat = 48
    static let landscapeDestinationCardWidth: CGFloat = 148
    static let landscapeDestinationCardHeight: CGFloat = 112

    static let gpxDetailsCollapsedDetentHeight: CGFloat = 240
    static let gpxDetailsExpandedDetentHeight: CGFloat = 382
    static let gpxDetailsExpandedWithEndDetentHeight: CGFloat = 412
    static let gpxDetailsPadExpandedDetentHeight: CGFloat = 382
    static let gpxDetailsPadExpandedWithEndDetentHeight: CGFloat = 442
    static let gpxDetailsJelSymbolSize: CGFloat = 20
    static let gpxDetailsReverseButtonHeight: CGFloat = 80
    static let gpxDetailsCollapsedReverseButtonHeight: CGFloat = 62

    static func gpxDetailsCollapsedHeight(isReversible: Bool) -> CGFloat {
        gpxDetailsCollapsedDetentHeight + (isReversible ? gpxDetailsCollapsedReverseButtonHeight : 0)
    }

    static func gpxDetailsExpandedHeight(hasEnd: Bool, isReversible: Bool, isPad: Bool) -> CGFloat {
        let reverseButtonHeight = isReversible ? gpxDetailsReverseButtonHeight : 0
        if isPad {
            return (hasEnd ? gpxDetailsPadExpandedWithEndDetentHeight : gpxDetailsPadExpandedDetentHeight)
                + reverseButtonHeight
        }
        return (hasEnd ? gpxDetailsExpandedWithEndDetentHeight : gpxDetailsExpandedDetentHeight) + reverseButtonHeight
    }

    static let placeDetailsDetentHeight: CGFloat = 215

    static let discoverDetentHeight: CGFloat = 400

    static let layersDetentHeight: CGFloat = 360
    static let layersPadDetentHeight: CGFloat = 400

    static let mapLabelScaleDetentHeight: CGFloat = 200

    static let routePlannerDetentHeight: CGFloat = 520
    static let routePlannerMinimizedDetentHeight: CGFloat = 190

    static let whatsNewDetentHeight: CGFloat = 350
}
