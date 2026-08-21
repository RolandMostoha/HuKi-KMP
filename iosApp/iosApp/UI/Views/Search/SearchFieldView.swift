import Shared
import SwiftUI

struct SearchFieldView: View {
    let strings: Strings
    @Binding var text: String
    var isFocused: FocusState<Bool>.Binding
    var accessibilityIdentifier: String?

    var body: some View {
        TextField(
            "",
            text: $text,
            prompt: Text(strings.get(id: SharedRes.strings().search_input_placeholder))
                .foregroundStyle(Color(.secondaryLabel))
        )
        .font(.system(size: 18, weight: .regular))
        .foregroundStyle(.secondary)
        .submitLabel(.search)
        .textContentType(.fullStreetAddress)
        .focused(isFocused)
        .tint(Color(SharedRes.colors().primary.getUIColor()))
        .padding(.leading, 48)
        .padding(.trailing, text.isEmpty ? 16 : 44)
        .padding(.vertical, 14)
        .glassBackground()
        .overlay(alignment: .leading) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.primary)
                .padding(.leading, 18)
        }
        .overlay(alignment: .trailing) {
            if !text.isEmpty {
                clearButton
            }
        }
        .contentShape(Capsule())
        .onTapGesture {
            isFocused.wrappedValue = true
        }
        .identifier(accessibilityIdentifier)
    }

    private var clearButton: some View {
        Button(action: { text = "" }, label: {
            Image(systemName: "xmark.circle.fill")
                .font(.system(size: 20))
                .foregroundStyle(.primary)
                .padding(.trailing, 14)
        })
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_clear_text))
    }
}

private struct SearchFieldPreview: View {
    @State private var text = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        SearchFieldView(strings: Strings(), text: $text, isFocused: $isFocused)
            .padding()
    }
}

#Preview {
    SearchFieldPreview()
}
