@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

extension MapView {
    private static let standardLabelConfigs = [
        "showPlaceLabels",
        "showRoadLabels",
        "showPointOfInterestLabels",
        "showTransitLabels"
    ]
    private static let classicStyleSource = "composite"

    /// The landscape overview is a picker, so the base map's own labels would only compete with landscape names.
    var isBaseLabelsHidden: Bool {
        guard let landscapes = uiState.mapUiState.landscapes, landscapes.grouping == .landscape else { return false }
        return landscapes.selectedLandscapeId == nil
    }

    func syncBaseLabelsVisibility(proxy: MapProxy) async {
        guard let map = proxy.map else { return }
        let isHidden = isBaseLabelsHidden
        syncBaseLabels(isHidden: isHidden, map: map)
        let styleLoads = AsyncStream<Void> { continuation in
            let cancelable = map.onStyleLoaded.observe { _ in
                continuation.yield(())
            }
            continuation.onTermination = { _ in cancelable.cancel() }
        }
        for await _ in styleLoads {
            // Switching the base layer or appearance reloads the style with its own label visibility
            hiddenBaseLabelLayerIds = nil
            syncBaseLabels(isHidden: isHidden, map: map)
        }
    }

    // Restores only the labels it hid, as the style keeps some of its own labels hidden
    private func syncBaseLabels(isHidden: Bool, map: MapboxMap) {
        switch (isHidden, hiddenBaseLabelLayerIds) {
        case (true, nil):
            hiddenBaseLabelLayerIds = Self.hideBaseLabels(map: map)
        case (false, let layerIds?):
            Self.showBaseLabels(layerIds, map: map)
            hiddenBaseLabelLayerIds = nil
        default:
            break
        }
    }

    private static func hideBaseLabels(map: MapboxMap) -> [String] {
        setStandardLabelsVisible(false, map: map)
        let layerIds = map.allLayerIdentifiers
            .filter { $0.type == .symbol }
            .map(\.id)
            .filter { map.layerProperty(for: $0, property: "source").value as? String == classicStyleSource }
            .filter { map.layerProperty(for: $0, property: "visibility").value as? String != "none" }
        for layerId in layerIds {
            try? map.setLayerProperty(for: layerId, property: "visibility", value: "none")
        }
        return layerIds
    }

    private static func showBaseLabels(_ layerIds: [String], map: MapboxMap) {
        setStandardLabelsVisible(true, map: map)
        for layerId in layerIds {
            try? map.setLayerProperty(for: layerId, property: "visibility", value: "visible")
        }
    }

    private static func setStandardLabelsVisible(_ isVisible: Bool, map: MapboxMap) {
        for config in standardLabelConfigs {
            try? map.setStyleImportConfigProperty(for: "basemap", config: config, value: isVisible)
        }
    }
}
