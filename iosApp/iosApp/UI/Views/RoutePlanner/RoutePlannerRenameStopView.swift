import Shared
import SwiftUI

struct RoutePlannerRenameStopView: View {
    let strings: Strings
    let stopNumber: Int
    let resolvedName: String
    let address: String?
    let onDone: (_ name: String, _ comment: String) -> Void
    let onCancel: () -> Void

    @State private var name: String
    @State private var comment: String
    @FocusState private var isNameFocused: Bool
    @State private var contentHeight: CGFloat = 300

    private let primaryColor = Color(SharedRes.colors().primary.getUIColor())
    private let fieldShape = RoundedRectangle(cornerRadius: 24, style: .continuous)
    private let sectionInset: CGFloat = 16
    private let clearButtonInset: CGFloat = 24

    init(
        strings: Strings,
        stopNumber: Int,
        resolvedName: String,
        currentName: String,
        currentComment: String,
        address: String?,
        onDone: @escaping (_ name: String, _ comment: String) -> Void,
        onCancel: @escaping () -> Void
    ) {
        self.strings = strings
        self.stopNumber = stopNumber
        self.resolvedName = resolvedName
        self.address = address
        self.onDone = onDone
        self.onCancel = onCancel
        _name = State(initialValue: currentName)
        _comment = State(initialValue: currentComment)
    }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 0) {
                sectionHeader(strings.get(id: SharedRes.strings().route_planner_rename_name))
                nameField
                if let address {
                    Text(strings.get(id: SharedRes.strings().route_planner_rename_address, args: [address]))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                        .padding(.horizontal, sectionInset)
                        .padding(.top, 8)
                }
                sectionHeader(strings.get(id: SharedRes.strings().route_planner_rename_comment))
                    .padding(.top, 16)
                commentField
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
            .fixedSize(horizontal: false, vertical: true)
            // The navigation bar is the content's top safe area, so the sheet fits both
            .onGeometryChange(for: CGFloat.self) { $0.size.height + $0.safeAreaInsets.top } action: { height in
                contentHeight = height
            }
            .frame(maxHeight: .infinity, alignment: .top)
            .navigationTitle(strings.get(id: SharedRes.strings().route_planner_rename_title, args: [stopNumber]))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { cancelButton }
                ToolbarItem(placement: .confirmationAction) { doneButton }
            }
        }
        .presentationDetents([.height(contentHeight)])
        .presentationDragIndicator(.hidden)
        .onAppear { isNameFocused = true }
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_RENAME_SHEET)
    }

    private func sectionHeader(_ text: String) -> some View {
        Text(text)
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(.secondary)
            .padding(.horizontal, sectionInset)
            .padding(.bottom, 8)
    }

    private var nameField: some View {
        TextField(resolvedName, text: $name)
            .focused($isNameFocused)
            .submitLabel(.done)
            .onSubmit { onDone(name, comment) }
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_RENAME_NAME)
            .padding(.trailing, name.isEmpty ? 0 : clearButtonInset)
            .fieldStyle(shape: fieldShape)
            .overlay(alignment: .trailing) {
                if !name.isEmpty {
                    clearButton { name = "" }
                }
            }
    }

    private var commentField: some View {
        TextField(
            strings.get(id: SharedRes.strings().route_planner_rename_comment_placeholder),
            text: $comment,
            axis: .vertical
        )
        .lineLimit(2...4)
        .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_RENAME_COMMENT)
        .padding(.trailing, comment.isEmpty ? 0 : clearButtonInset)
        .fieldStyle(shape: fieldShape)
        .overlay(alignment: .trailing) {
            if !comment.isEmpty {
                clearButton { comment = "" }
            }
        }
    }

    private func clearButton(action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: "xmark.circle.fill")
                .font(.system(size: 18))
                .foregroundStyle(.tertiary)
                .padding(.trailing, 14)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_clear_text))
    }

    @ViewBuilder
    private var cancelButton: some View {
        if #available(iOS 26, *) {
            Button(role: .close, action: onCancel)
                .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_rename_cancel))
        } else {
            Button(strings.get(id: SharedRes.strings().route_planner_rename_cancel), action: onCancel)
        }
    }

    @ViewBuilder
    private var doneButton: some View {
        if #available(iOS 26, *) {
            Button(role: .confirm) { onDone(name, comment) }
                .tint(primaryColor)
                .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_rename_save))
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_RENAME_DONE)
        } else {
            Button(strings.get(id: SharedRes.strings().route_planner_rename_save)) { onDone(name, comment) }
                .fontWeight(.semibold)
                .tint(primaryColor)
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_RENAME_DONE)
        }
    }
}

private extension View {
    // Solid like grouped list rows, since the glass sheet behind would wash the fields out
    func fieldStyle(shape: RoundedRectangle) -> some View {
        padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color(.secondarySystemGroupedBackground), in: shape)
    }
}

#Preview {
    Color.green.opacity(0.3)
        .ignoresSafeArea()
        .sheet(isPresented: .constant(true)) {
            RoutePlannerRenameStopView(
                strings: Strings(),
                stopNumber: 1,
                resolvedName: "Háromkő-hegy, Pilisi Parkerdő",
                currentName: "Háromkő-hegy, Pilisi Parkerdő",
                currentComment: "",
                address: "Háromkő-hegy, Pilisi Parkerdő, Pilisszentkereszt, 2099",
                onDone: { _, _ in },
                onCancel: {}
            )
        }
}
