import CoreLocation
import Shared
import SwiftUI

struct MapCameraDebugOverlay: View {
    let zoom: Double
    let center: CLLocationCoordinate2D
    let onMoveCamera: (CameraPosition) -> Void
    let onClose: () -> Void
    @State private var editedInput: String?
    @FocusState private var isEditing: Bool

    private var liveInput: String {
        String(format: "%.5f,%.5f,%.2f", center.latitude, center.longitude, zoom)
    }

    // While editing, show the user's buffer; otherwise mirror the live camera so the value stays copyable.
    private var fieldText: Binding<String> {
        Binding(get: { editedInput ?? liveInput }, set: { editedInput = $0 })
    }

    var body: some View {
        ZStack(alignment: .top) {
            Image(systemName: "plus")
                .font(.system(size: 28, weight: .thin))
                .foregroundStyle(.red)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .allowsHitTesting(false)
            HStack(spacing: 8) {
                TextField("lat,lon,zoom", text: fieldText)
                    .textFieldStyle(.roundedBorder)
                    .font(.system(size: 11, design: .monospaced))
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .keyboardType(.numbersAndPunctuation)
                    .frame(width: 190)
                    .focused($isEditing)
                    .onSubmit(moveCamera)
                Button(action: moveCamera) {
                    Image(systemName: "location.magnifyingglass")
                }
                .buttonStyle(.borderedProminent)
                Button(action: onClose) {
                    Image(systemName: "xmark")
                }
                .buttonStyle(.bordered)
            }
            .padding(6)
            .background(.black.opacity(0.6), in: RoundedRectangle(cornerRadius: 8))
            .padding(.top, 8)
        }
        .onChange(of: isEditing) { _, editing in
            editedInput = editing ? liveInput : nil
        }
    }

    private func moveCamera() {
        guard let position = CameraTargetParser.shared.parse(value: editedInput ?? liveInput) else { return }
        onMoveCamera(position)
        // Drop the edit buffer + focus so the field resumes mirroring the live camera.
        editedInput = nil
        isEditing = false
    }
}

#Preview {
    MapCameraDebugOverlay(
        zoom: 14.5,
        center: CLLocationCoordinate2D(latitude: 47.7167, longitude: 18.9139),
        onMoveCamera: { _ in },
        onClose: {}
    )
}
