import SwiftUI
import Shared

extension View {
    @ViewBuilder
    func glassIfAvailable() -> some View {
        if #available(iOS 26, *) {
            self.buttonStyle(.glass)
        } else {
            self.buttonStyle(.borderedProminent)
        }
    }
}

extension Image {
    init(resource: KeyPath<SharedRes.images, Shared.ImageResource>) {
        let imageResource = SharedRes.images()[keyPath: resource]
        self.init(imageResource.assetImageName, bundle: imageResource.bundle)
    }
}

extension UIImage {
    func resized(to newSize: CGSize) -> UIImage {
        return UIGraphicsImageRenderer(size: newSize).image { _ in
            self.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }
}
