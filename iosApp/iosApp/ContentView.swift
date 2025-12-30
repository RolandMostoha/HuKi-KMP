import MapboxMaps
import Shared
import SwiftUI

struct ContentView: View {
    @State private var showContent = false
    var body: some View {
        VStack {
            Button("Click me!") {
                withAnimation {
                    showContent = !showContent
                }
            }
            Map(
                initialViewport: .camera(
                    center: CLLocationCoordinate2D(
                        latitude: MapConstants.shared.BUDAPEST_LATITUDE,
                        longitude: MapConstants.shared.BUDAPEST_LONGITUDE
                    ),
                    zoom: MapConstants.shared.HUNGARY_ZOOM_LEVEL,
                    bearing: 0,
                    pitch: 0
                )
            )
            .mapStyle(.outdoors)
            .ignoresSafeArea()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
