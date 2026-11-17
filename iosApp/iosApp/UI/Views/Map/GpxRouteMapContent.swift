@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct GpxRouteMapContent: MapContent {
    let gpxDetails: GpxDetails
    var colorScheme: ColorScheme = .light
    var slopeColorsVisible: Bool = false

    private var slopeSourceId: String { "\(gpxDetails.layerId)_slope" }

    var body: some MapContent {
        GeoJSONSource(id: gpxDetails.layerId)
            .data(.feature(Feature(geometry: .lineString(gpxDetails.locations.lineString))))

        if slopeColorsVisible, gpxDetails.hasSlopeSegments {
            GeoJSONSource(id: slopeSourceId)
                .data(.featureCollection(SlopeFeatureCache.shared.features(for: gpxDetails, colorScheme: colorScheme)))
            lineLayer(id: "\(gpxDetails.layerId)_slope_casing", source: gpxDetails.layerId)
                .lineColor(SharedRes.colors().markerStroke.getUIColor().resolved(for: colorScheme))
            lineLayer(id: slopeSourceId, source: slopeSourceId)
                .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH - 2 * SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
                .lineColor(Exp(.get) { SlopeSegmentFeature.colorProperty })
        } else {
            lineLayer(id: gpxDetails.layerId, source: gpxDetails.layerId)
                .lineColor(SharedRes.colors().primary.getUIColor().resolved(for: colorScheme))
                .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
                .lineBorderColor(SharedRes.colors().mapStroke.getUIColor().resolved(for: colorScheme))
                .lineBorderColorUseTheme(.none)
        }

        RouteDirectionMapContent(
            sourceId: gpxDetails.layerId,
            layerId: "\(gpxDetails.layerId)_direction",
            colorScheme: colorScheme
        )
    }

    private func lineLayer(id: String, source: String) -> LineLayer {
        LineLayer(id: id, source: source)
            .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
            .lineCap(.round)
            .lineJoin(.round)
            .lineColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }

}

private enum SlopeSegmentFeature {
    static let colorProperty = "color"
}

// The map body is re-evaluated on every UI state change, e.g. each location update
private final class SlopeFeatureCache: @unchecked Sendable {
    static let shared = SlopeFeatureCache()

    private let lock = NSLock()
    private var key: String?
    private var cached = FeatureCollection(features: [])

    func features(for gpxDetails: GpxDetails, colorScheme: ColorScheme) -> FeatureCollection {
        lock.lock()
        defer { lock.unlock() }
        let key = "\(gpxDetails.layerId)_\(colorScheme)"
        if key == self.key { return cached }

        let flatColor = SharedRes.colors().primary.getUIColor().resolved(for: colorScheme).rgb
        let colorScale = SlopeColorScale(flatColor: flatColor)
        cached = FeatureCollection(features: gpxDetails.slopeSegments.map { segment in
            var feature = Feature(geometry: .lineString(segment.locations.lineString))
            feature.properties = [
                SlopeSegmentFeature.colorProperty: .string(colorScale.colorHex(slopePercent: segment.slopePercent))
            ]
            return feature
        })
        self.key = key
        return cached
    }
}

struct RouteDirectionMapContent: MapContent {
    let sourceId: String
    let layerId: String
    let colorScheme: ColorScheme

    var body: some MapContent {
        StyleImage(id: RouteDirectionArrow.imageId, image: RouteDirectionArrow.image, sdf: true)

        SymbolLayer(id: layerId, source: sourceId)
            .minZoom(Double(SharedDimens.shared.GPX_DIRECTION_ARROW_MIN_ZOOM))
            .symbolPlacement(.line)
            .symbolSpacing(SharedDimens.shared.GPX_DIRECTION_ARROW_SPACING)
            .iconImage(RouteDirectionArrow.imageId)
            // An SDF icon takes its color from iconColor, which can opt out of the dark map's color grading
            .iconColor(SharedRes.colors().mapStroke.getUIColor().resolved(for: colorScheme))
            .iconColorUseTheme(.none)
            .iconHaloColor(SharedRes.colors().mapStrokeContrast.getUIColor().resolved(for: colorScheme))
            .iconHaloColorUseTheme(.none)
            .iconHaloWidth(SharedDimens.shared.GPX_DIRECTION_ARROW_HALO_WIDTH)
            .iconEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            .iconRotationAlignment(.map)
            // Upright icons flip on westbound segments, which would point arrows the wrong way
            .iconKeepUpright(false)
            .iconAllowOverlap(true)
            .iconIgnorePlacement(true)
    }
}

private enum RouteDirectionArrow {
    static let imageId = "route_direction_arrow"

    static let image: UIImage = {
        let scale = UIGraphicsImageRendererFormat.preferred().scale
        let sdf = DirectionArrowSdf.companion.create(density: Float(scale))
        let width = Int(sdf.widthPx)
        let height = Int(sdf.heightPx)
        var pixels = [UInt8](repeating: 0, count: width * height * 4)
        for index in 0..<width * height {
            // Premultiplied white, so every channel equals the alpha
            let alpha = UInt8(sdf.alpha.get(index: Int32(index)))
            pixels[index * 4] = alpha
            pixels[index * 4 + 1] = alpha
            pixels[index * 4 + 2] = alpha
            pixels[index * 4 + 3] = alpha
        }
        guard
            let provider = CGDataProvider(data: Data(pixels) as CFData),
            let cgImage = CGImage(
                width: width,
                height: height,
                bitsPerComponent: 8,
                bitsPerPixel: 32,
                bytesPerRow: width * 4,
                space: CGColorSpaceCreateDeviceRGB(),
                bitmapInfo: CGBitmapInfo(rawValue: CGImageAlphaInfo.premultipliedLast.rawValue),
                provider: provider,
                decode: nil,
                shouldInterpolate: true,
                intent: .defaultIntent
            )
        else { return UIImage() }
        return UIImage(cgImage: cgImage, scale: scale, orientation: .up)
    }()
}
