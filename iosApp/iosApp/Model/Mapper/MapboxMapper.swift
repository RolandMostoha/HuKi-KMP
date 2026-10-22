import MapboxMaps
import Shared
import SwiftUI

extension Shared.Location {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(
            latitude: latitude,
            longitude: longitude
        )
    }
}

extension CLLocationCoordinate2D {
    var location: Shared.Location {
        Shared.Location(latitude: latitude, longitude: longitude, altitude: nil)
    }
}

extension CameraState {
    var cameraPosition: Shared.CameraPosition {
        Shared.CameraPosition(
            location: center.location,
            zoom: zoom,
            bearing: bearing,
            pitch: pitch
        )
    }
}

extension Shared.Location {
    var id: String {
        String(format: "%.6f,%.6f,%.2f", latitude, longitude, (altitude?.doubleValue ?? 0.0))
    }
}

extension Array where Element == Shared.Location {
    var coordinates: [CLLocationCoordinate2D] {
        map(\.coordinate)
    }
}

extension Array where Element == Shared.Location {
    var lineString: LineString {
        LineString(self.coordinates)
    }
}

extension Shared.ContentPadding {
    func edgeInsets(
        isLandscape: Bool,
        routePlannerSheetHeight: CGFloat,
        listSheetHeight: CGFloat
    ) -> SwiftUI.EdgeInsets {
        switch self {
        case .mapGpx:
            return isLandscape ? Dimens.gpxContentPaddingLandscape : Dimens.gpxContentPaddingPortrait
        case .mapPlaceDetails:
            return isLandscape
                ? Dimens.placeDetailsContentPaddingLandscape
                : Dimens.placeDetailsContentPaddingPortrait
        case .mapRoutePlanner:
            return isLandscape
                ? Dimens.routePlannerContentPaddingLandscape
                : Dimens.routePlannerContentPaddingPortrait(sheetHeight: routePlannerSheetHeight)
        case .mapOkt:
            return isLandscape
                ? Dimens.oktContentPaddingLandscape
                : Dimens.oktContentPaddingPortrait(sheetHeight: listSheetHeight)
        case .mapOktStarted:
            return isLandscape ? Dimens.oktContentPaddingLandscape : Dimens.oktStartedContentPaddingPortrait
        case .mapLandscapes:
            return isLandscape
                ? Dimens.landscapesContentPaddingLandscape
                : Dimens.landscapesContentPaddingPortrait(sheetHeight: listSheetHeight)
        case .mapLandscapesDestination:
            return isLandscape
                ? Dimens.landscapesContentPaddingLandscape
                : Dimens.landscapesDestinationContentPaddingPortrait(sheetHeight: listSheetHeight)
        }
    }
}

extension Shared.BaseLayer {

    func mapStyle(isDarkMode: Bool) -> MapStyle {
        let lightPreset: StandardLightPreset = isDarkMode ? .night : .day

        switch self {
        case .outdoors:
            return .outdoors
        case .city:
            return .standard(lightPreset: lightPreset)
        case .satellite:
            return .standardSatellite(lightPreset: lightPreset)
        }
    }
}

extension Shared.ImageResource {

    func appearanceImage(for colorScheme: ColorScheme) -> UIImage {
        let traits = UITraitCollection(userInterfaceStyle: colorScheme == .dark ? .dark : .light)
        let image = self.toUIImage()!
        return image.imageAsset?.image(with: traits) ?? image
    }

    func annotationImage(for colorScheme: ColorScheme) -> PointAnnotation.Image {
        let isDark = colorScheme == .dark
        return PointAnnotation.Image(
            image: self.appearanceImage(for: colorScheme),
            name: "\(self.assetImageName)-\(isDark ? "dark" : "light")"
        )
    }
}

// Drawing the composite marker is costly and map content re-evaluates on every state change.
private let jelMarkerImageCache = NSCache<NSString, UIImage>()

extension JelSymbol {

    func markerImage(background: Shared.ImageResource, for colorScheme: ColorScheme) -> PointAnnotation.Image {
        let appearance = colorScheme == .dark ? "dark" : "light"
        let name = "\(background.assetImageName)-\(icon.assetImageName)-\(appearance)"
        if let cached = jelMarkerImageCache.object(forKey: name as NSString) {
            return PointAnnotation.Image(image: cached, name: name)
        }
        let backgroundImage = background.appearanceImage(for: colorScheme)
        let symbolImage = icon.toUIImage()!
        let size = backgroundImage.size
        let symbolSide = min(size.width, size.height) * SharedDimens.shared.GPX_JEL_SYMBOL_MARKER_RATIO
        let format = UIGraphicsImageRendererFormat()
        format.scale = backgroundImage.scale
        let image = UIGraphicsImageRenderer(size: size, format: format).image { _ in
            backgroundImage.draw(in: CGRect(origin: .zero, size: size))
            symbolImage.draw(
                in: CGRect(
                    x: (size.width - symbolSide) / 2,
                    y: (size.height - symbolSide) / 2,
                    width: symbolSide,
                    height: symbolSide
                )
            )
        }
        jelMarkerImageCache.setObject(image, forKey: name as NSString)
        return PointAnnotation.Image(image: image, name: name)
    }
}
