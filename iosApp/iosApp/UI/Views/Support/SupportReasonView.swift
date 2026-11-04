import Shared
import SwiftUI

struct SupportReasonView: View {
    let title: String
    let message: String
    @Binding var isExpanded: Bool
    var showDivider = true

    var body: some View {
        VStack(spacing: 0) {
            if showDivider {
                Divider()
            }
            DisclosureGroup(isExpanded: $isExpanded) {
                Text(message)
                    .font(.subheadline)
                    .lineSpacing(4)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.bottom, 12)
            } label: {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(.primary)
            }
            .disclosureGroupStyle(SupportReasonDisclosureStyle())
        }
        .padding(.top, showDivider ? 16 : 8)
    }
}

private struct SupportReasonDisclosureStyle: DisclosureGroupStyle {
    func makeBody(configuration: Configuration) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(
                action: {
                    withAnimation(.smooth(duration: 0.3)) {
                        configuration.isExpanded.toggle()
                    }
                },
                label: {
                    HStack {
                        configuration.label
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Image(systemName: "chevron.down")
                            .font(.footnote.weight(.semibold))
                            .foregroundStyle(.tertiary)
                            .rotationEffect(.degrees(configuration.isExpanded ? 180 : 0))
                            .accessibilityHidden(true)
                    }
                    .frame(minHeight: 48)
                    .padding(.top, 4)
                    .contentShape(Rectangle())
                }
            )
            .buttonStyle(.plain)
            .accessibilityAddTraits(.isHeader)
            if configuration.isExpanded {
                configuration.content
                    .contentShape(Rectangle())
                    .onTapGesture {
                        withAnimation(.smooth(duration: 0.3)) {
                            configuration.isExpanded = false
                        }
                    }
                    .transition(
                        .asymmetric(
                            insertion: .opacity.combined(with: .offset(y: -8)),
                            removal: .opacity.animation(.easeOut(duration: 0.12))
                        )
                    )
            }
        }
    }
}

#Preview {
    @Previewable @State var isExpanded = true
    SupportReasonView(
        title: "Miért csinálom?",
        message: "Az appot hobbiból, szabadidőmben fejlesztem.",
        isExpanded: $isExpanded
    )
    .padding()
}
