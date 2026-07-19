import Shared
import SwiftUI

struct WhatsNewSheetView: View {
    let strings: Strings
    let whatsNew: WhatsNew
    let onDismissRequest: () -> Void

    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let primaryContainer = Color(SharedRes.colors().primaryContainer.getUIColor())
    private let monthYearText: String
    private let noteLines: [String]

    init(strings: Strings, whatsNew: WhatsNew, onDismissRequest: @escaping () -> Void) {
        self.strings = strings
        self.whatsNew = whatsNew
        self.onDismissRequest = onDismissRequest
        self.monthYearText = LocalizedDateFormatter().formatMonthYear(date: whatsNew.releaseDate)
        self.noteLines = WhatsNewMapperKt.toReleaseNoteLines(strings.get(desc: whatsNew.releaseNotes))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 13) {
            header
            notesCard
            if let message = whatsNew.message {
                messageCard(message)
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .padding(.bottom, 16)
        .accessibilityIdentifier(TestTags.shared.WHATS_NEW_SHEET)
    }

    private var header: some View {
        HStack(alignment: .center, spacing: 13) {
            Image(uiImage: SharedRes.images().ic_app_icon.toUIImage()!)
                .resizable()
                .scaledToFit()
                .frame(width: 58, height: 58)
                .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                .shadow(color: .black.opacity(0.15), radius: 3, x: 0, y: 2)
                .accessibilityLabel(strings.get(id: SharedRes.strings().whats_new_a11y_app_icon))
            VStack(alignment: .leading, spacing: 6) {
                Text(strings.get(id: SharedRes.strings().whats_new_title))
                    .font(.title2.weight(.bold))
                HStack(spacing: 10) {
                    versionPill
                    Text(monthYearText)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            Spacer()
            closeButton
        }
    }

    private var versionPill: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(primary)
                .frame(width: 6, height: 6)
            Text("v\(whatsNew.version)")
                .font(.caption.weight(.semibold))
                .foregroundStyle(primary)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 6)
        .background(primary.opacity(0.15), in: .capsule)
    }

    private var closeButton: some View {
        Button(action: onDismissRequest) {
            Image(systemName: "xmark")
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(.primary)
                .padding(10)
                .background(Circle().fill(Color(.systemGray5)))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(TestTags.shared.WHATS_NEW_CLOSE_BUTTON)
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
    }

    private var notesCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach(noteLines, id: \.self) { note in
                HStack(alignment: .center, spacing: 13) {
                    Circle()
                        .fill(primary)
                        .frame(width: 6, height: 6)
                    Text(note)
                        .font(.subheadline)
                    Spacer(minLength: 0)
                }
            }
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private func messageCard(_ message: WhatsNewMessage) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 8) {
                Image(systemName: "heart.fill")
                    .font(.system(size: 16))
                    .foregroundStyle(primary)
                Text(strings.get(desc: message.title).uppercased())
                    .font(.caption.weight(.bold))
                    .foregroundStyle(primary)
            }
            Text(strings.get(desc: message.body))
                .font(.footnote)
                .italic()
                .foregroundStyle(.secondary)
            Text(strings.get(id: SharedRes.strings().whats_new_message_signature))
                .font(.footnote)
                .italic()
                .foregroundStyle(primary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(primaryContainer, in: RoundedRectangle(cornerRadius: 13, style: .continuous))
        .padding(.leading, 3)
        .background(primary, in: RoundedRectangle(cornerRadius: 13, style: .continuous))
        .accessibilityIdentifier(TestTags.shared.WHATS_NEW_MESSAGE_CARD)
    }
}
