import Shared
import SwiftUI

extension Shared.ImageResource {
    /// Builds an `ImageResource` from an SF Symbol name.
    static func system(_ systemName: String) -> Shared.ImageResource {
        Shared.ImageResource(assetImageName: systemName, bundle: .main)
    }

    /// Renders the resource as a SwiftUI `Image`, preferring an SF Symbol over the moko asset.
    var swiftUIImage: Image {
        if UIImage(systemName: assetImageName) != nil {
            return Image(systemName: assetImageName)
        }
        return Image(assetImageName, bundle: bundle)
    }
}
