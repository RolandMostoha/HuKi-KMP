import Shared
import SwiftUI

extension MainView {
    func handleMainEffects(_ effect: MainUiEffects) {
        switch onEnum(of: effect) {
        case .navigateToAppSettings:
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        case .showGpxFilePicker:
            showFileImporter = true
        case .openMapsNavigation(let effect):
            MapsNavigator.openDirections(to: effect.location)
        case .routePlannerLocationPicked, .routePlannerStopDragged, .routePlannerMarkerClicked,
             .routePlannerStopDeselected:
            routePlannerMapEvent = Self.routePlannerEvent(for: effect).map { RoutePlannerMapEvent(event: $0) }
        case .shareGpxFile(let effect):
            gpxShareItem = ShareItem(url: URL(fileURLWithPath: effect.fileUri))
        case .openUrl(let effect):
            if let url = URL(string: effect.url) {
                UIApplication.shared.open(url)
            }
        case .navigateToDestinations:
            navigationPath.append(DestinationsRoute.destinations)
        case .navigateToGpxGuide:
            navigationPath.append(GpxGuideRoute.gpxGuide)
        }
    }

    private static func routePlannerEvent(for effect: MainUiEffects) -> RoutePlannerUiEvents? {
        switch onEnum(of: effect) {
        case .routePlannerLocationPicked(let effect):
            RoutePlannerUiEventsLocationAdded(location: effect.location)
        case .routePlannerStopDragged(let effect):
            RoutePlannerUiEventsStopDragged(from: effect.from, to: effect.to)
        case .routePlannerMarkerClicked(let effect):
            RoutePlannerUiEventsStopMarkerClicked(location: effect.location)
        case .routePlannerStopDeselected:
            RoutePlannerUiEventsStopDeselected.shared
        default:
            nil
        }
    }
}
