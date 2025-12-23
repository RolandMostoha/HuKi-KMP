import SwiftUI
import Shared
import MapboxMaps

struct ContentView: View {
    @State private var showContent = false
    var body: some View {
        VStack {
            Button("Click me!") {
                withAnimation {
                    showContent = !showContent
                }
            }
            
            let center = CLLocationCoordinate2D(latitude: MapConstants.shared.BUDAPEST_LATITUDE, longitude:  MapConstants.shared.BUDAPEST_LONGITUDE)
            Map(
                initialViewport: .camera(
                    center: center,
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
