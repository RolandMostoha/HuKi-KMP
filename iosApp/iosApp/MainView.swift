import MapboxMaps
import Shared
import SwiftUI

struct MainView: View {
    @ObservedObject private var mainViewModel = MainViewModelWrapper()

    var body: some View {
        VStack {
            Map(
                initialViewport: .camera(
                    center: CLLocationCoordinate2D(
                        latitude: mainViewModel.uiState.mapUiState.latitude,
                        longitude: mainViewModel.uiState.mapUiState.longitude
                    ),
                    zoom: mainViewModel.uiState.mapUiState.zoomLevel,
                    bearing: 0,
                    pitch: 0
                )
            )
            .mapStyle(.outdoors)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .ignoresSafeArea()
        .task {
            await mainViewModel.startObserving()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}
