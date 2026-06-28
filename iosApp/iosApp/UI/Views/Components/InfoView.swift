import Shared
import SwiftUI

struct InfoView: View {
    let strings: Strings
    let infoViewData: InfoViewData
    var primaryActionText: String?
    var onPrimaryActionClick: (() -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color(infoViewData.infoViewType.containerColor))
                    .frame(width: 100, height: 100)
                infoViewData.icon.swiftUIImage
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .foregroundStyle(Color(infoViewData.infoViewType.contentColor))
            }
            Text(strings.get(id: infoViewData.title))
                .font(.title3)
                .fontWeight(.semibold)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.top, 24)
                .padding(.horizontal, 16)
            Text(strings.get(id: infoViewData.message))
                .font(.body)
                .foregroundStyle(Color(.secondaryLabel))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.top, 16)
                .padding(.horizontal, 32)
            if let primaryActionText, let onPrimaryActionClick {
                Button(action: onPrimaryActionClick) {
                    Text(primaryActionText)
                        .font(.body)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 6)
                }
                .buttonStyle(.borderedProminent)
                .tint(Color(SharedRes.colors().primary.getUIColor()))
                .foregroundStyle(Color(SharedRes.colors().onPrimary.getUIColor()))
                .padding(.top, 24)
            }
        }
    }
}

private extension InfoViewType {
    var contentColor: UIColor {
        switch self {
        case .error: return SharedRes.colors().error.getUIColor()
        case .warning: return SharedRes.colors().warning.getUIColor()
        case .success: return SharedRes.colors().success.getUIColor()
        case .info: return SharedRes.colors().info.getUIColor()
        }
    }

    var containerColor: UIColor {
        switch self {
        case .error: return SharedRes.colors().errorContainer.getUIColor()
        case .warning: return SharedRes.colors().warningContainer.getUIColor()
        case .success: return SharedRes.colors().successContainer.getUIColor()
        case .info: return SharedRes.colors().infoContainer.getUIColor()
        }
    }
}
