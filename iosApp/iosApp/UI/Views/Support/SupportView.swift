import Shared
import SwiftUI

enum SupportRoute: Hashable {
    case support
}

struct SupportView: View {
    @StateObject private var holder = ViewModelHolder(KoinViewModelProvider.shared.getSupportViewModel)
    private var viewModel: SupportViewModel { holder.viewModel }
    @Environment(\.dismiss) private var dismiss
    @State private var expandedReasons: Set<Int> = []
    // Revealed only once the top is in view, where the supporter card appears
    @State private var supporterAnimal: SupportAnimal?
    @State private var celebrate = false
    // A celebration replaces the card, any other change animates within it
    @State private var celebrationId: Int32 = 0

    private let strings = Strings()
    private let cardColor = SwiftUI.Color(.secondarySystemGroupedBackground)

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            ScrollViewReader { proxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        Group {
                            if let supporterAnimal {
                                SupporterCardView(
                                    animal: supporterAnimal,
                                    celebrate: celebrate,
                                    choices: uiState.supporterChoices,
                                    onChoose: onSupporterAnimalChosen
                                )
                                .id(celebrationId)
                                .transition(
                                    .asymmetric(
                                        insertion: .scale(scale: 0.92).combined(with: .opacity),
                                        removal: .opacity.animation(.easeOut(duration: 0.1))
                                    )
                                )
                            } else {
                                introCard
                                    .transition(.opacity.animation(.easeOut(duration: 0.1)))
                            }
                        }
                        .id(Self.topId)
                        switch uiState.pricesState {
                        case .loading:
                            tiersLoadingView
                        case .unavailable:
                            pricesUnavailableView
                        case .loaded:
                            if let purchaseMessage = uiState.purchaseMessage {
                                SupportPurchaseMessageView(
                                    purchaseMessage: purchaseMessage,
                                    onDismiss: {
                                        viewModel.onEvent(event: SupportUiEventsPurchaseMessageDismissed.shared)
                                    }
                                )
                                .padding(.vertical, 24)
                                .frame(maxWidth: .infinity)
                                .background(cardColor, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
                                .padding(.top, 28)
                                .transition(.opacity)
                            }
                            SupportTierSectionView(
                                tiers: uiState.tiers.filter { $0.animal.type == .oneTime },
                                title: strings.get(
                                    id: supporterAnimal == nil
                                        ? SharedRes.strings().support_one_time_title
                                        : SharedRes.strings().support_one_time_more_title
                                ),
                                note: strings.get(id: SharedRes.strings().support_one_time_note_ios),
                                purchasingAnimal: uiState.purchasingAnimal,
                                onTierClick: onTierClick
                            )
                            .transition(.opacity)
                            SupportTierSectionView(
                                tiers: uiState.tiers.filter { $0.animal.type == .recurring },
                                title: strings.get(
                                    id: supporterAnimal == nil
                                        ? SharedRes.strings().support_recurring_title
                                        : SharedRes.strings().support_recurring_more_title
                                ),
                                note: strings.get(id: SharedRes.strings().support_recurring_note_ios),
                                purchasingAnimal: uiState.purchasingAnimal,
                                onTierClick: onTierClick
                            )
                            .transition(.opacity)
                            SupportManageSubscriptionsButton {
                                viewModel.onEvent(event: SupportUiEventsManageSubscriptionsClicked.shared)
                            }
                            .padding(.horizontal, 6)
                            .padding(.top, 12)
                        }
                        if supporterAnimal != nil {
                            aboutSection
                                .transition(.opacity)
                        }
                        SupportContactView(cardColor: cardColor)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 6)
                    .padding(.bottom, 48)
                    .readableWidth()
                    .animation(.default, value: uiState.pricesState)
                    .animation(.default, value: uiState.purchaseMessage)
                }
                .modifier(SupportAlerts(uiState: uiState, onEvent: { viewModel.onEvent(event: $0) }))
                .onAppear {
                    supporterAnimal = uiState.supporterAnimal
                    celebrationId = uiState.celebrationId
                }
                .onChange(
                    of: SupporterHeader(animal: uiState.supporterAnimal, celebrationId: uiState.celebrationId)
                ) { _, header in
                    revealSupporter(header, proxy: proxy)
                }
            }
        }
        .task {
            viewModel.onEvent(event: SupportUiEventsScreenViewed.shared)
        }
        .background(SwiftUI.Color(.systemGroupedBackground))
        .accessibilityIdentifier(TestTags.shared.SUPPORT_SCREEN_ROOT)
        .navigationTitle(strings.get(id: SharedRes.strings().support_title))
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
    }

    private static let topId = "supportTop"

    private func revealSupporter(_ header: SupporterHeader, proxy: ScrollViewProxy) {
        guard let animal = header.animal else {
            supporterAnimal = nil
            return
        }
        guard header.celebrationId != celebrationId else {
            celebrate = false
            withAnimation(.easeInOut(duration: 0.35)) {
                supporterAnimal = animal
            }
            return
        }
        celebrate = true
        Task { @MainActor in
            withAnimation(.easeInOut(duration: 0.35)) {
                proxy.scrollTo(Self.topId, anchor: .top)
            }
            try? await Task.sleep(for: .milliseconds(350))
            withAnimation(.spring(duration: 0.5, bounce: 0.2)) {
                celebrationId = header.celebrationId
                supporterAnimal = animal
            }
        }
    }

    private var introCard: some View {
        VStack(alignment: .leading, spacing: 0) {
            SupportAnimalBadgesView(ringColor: cardColor)
            Text(strings.get(id: SharedRes.strings().support_intro_title))
                .font(.title.weight(.bold))
                .foregroundStyle(.primary)
                .padding(.top, 14)
            Text(strings.get(id: SharedRes.strings().support_intro_message))
                .font(.subheadline)
                .lineSpacing(4)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 8)
            reasonList(showFirstDivider: true)
        }
        .padding(.top, 22)
        .padding(.horizontal, 20)
        .padding(.bottom, 8)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(cardColor, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
    }

    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            SectionHeaderView(title: strings.get(id: SharedRes.strings().support_about_title))
                .padding(.horizontal, -10)
                .padding(.top, 28)
                .padding(.bottom, 10)
                .accessibilityAddTraits(.isHeader)
            VStack(alignment: .leading, spacing: 0) {
                reasonList(showFirstDivider: false)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 8)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(cardColor, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
        }
    }

    private func reasonList(showFirstDivider: Bool) -> some View {
        ForEach(Array(SupportSection.allCases.enumerated()), id: \.offset) { index, section in
            SupportReasonView(
                title: strings.get(id: section.title),
                message: strings.get(id: section.message),
                isExpanded: reasonExpandedBinding(index, section: section),
                showDivider: showFirstDivider || index > 0
            )
            .accessibilityIdentifier(TestTags.shared.supportReason(index: Int32(index)))
        }
    }

    private func onSupporterAnimalChosen(_ animal: SupportAnimal) {
        viewModel.onEvent(event: SupportUiEventsSupporterAnimalChosen(animal: animal))
    }

    private func onTierClick(_ animal: SupportAnimal) {
        viewModel.onEvent(event: SupportUiEventsTierClicked(animal: animal))
    }

    private func reasonExpandedBinding(_ index: Int, section: SupportSection) -> Binding<Bool> {
        Binding(
            get: { expandedReasons.contains(index) },
            set: { isExpanded in
                if isExpanded {
                    if expandedReasons.insert(index).inserted {
                        viewModel.onEvent(event: SupportUiEventsSectionOpened(section: section))
                    }
                } else {
                    expandedReasons.remove(index)
                }
            }
        )
    }
}

private struct SupporterHeader: Equatable {
    let animal: SupportAnimal?
    let celebrationId: Int32
}

private extension SupportView {
    var backButton: some View {
        Button(
            action: { dismiss() },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().support_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.SUPPORT_BACK_BUTTON)
    }

    var tiersLoadingView: some View {
        ProgressView()
            .controlSize(.large)
            .frame(maxWidth: .infinity, minHeight: 220)
            .transition(.opacity)
            .accessibilityLabel(strings.get(id: SharedRes.strings().support_a11y_loading))
            .accessibilityIdentifier(TestTags.shared.SUPPORT_LOADING)
    }

    var pricesUnavailableView: some View {
        InfoView(
            strings: strings,
            infoViewData: InfoViewData(
                infoViewType: .error,
                icon: SharedRes.images().ic_error,
                title: SharedRes.strings().support_prices_unavailable_title,
                message: SharedRes.strings().support_prices_unavailable
            ),
            primaryActionText: strings.get(id: SharedRes.strings().support_retry),
            onPrimaryActionClick: {
                viewModel.onEvent(event: SupportUiEventsRetryClicked.shared)
            }
        )
        .padding(.top, 40)
        .transition(.opacity)
        .accessibilityIdentifier(TestTags.shared.SUPPORT_PRICES_UNAVAILABLE)
    }
}

#Preview {
    NavigationStack {
        SupportView()
    }
}

private struct SupportAlerts: ViewModifier {
    let uiState: SupportUiState
    let onEvent: (SupportUiEvents) -> Void

    @State private var showAlreadyPurchased = false
    @State private var showScheduled = false

    private let strings = Strings()

    func body(content: Content) -> some View {
        content
            .alert(
                strings.get(id: SharedRes.strings().support_already_purchased_title),
                isPresented: $showAlreadyPurchased,
                presenting: uiState.alreadyPurchasedAnimal
            ) { _ in
                Button(strings.get(id: SharedRes.strings().support_purchase_message_dismiss), role: .cancel) {
                    onEvent(SupportUiEventsAlreadyPurchasedDialogDismissed.shared)
                }
            } message: { animal in
                Text(
                    strings.get(
                        id: SharedRes.strings().support_already_purchased_message,
                        args: [strings.get(id: animal.nameRes)]
                    )
                )
            }
            .onChange(of: uiState.alreadyPurchasedAnimal != nil) { _, newValue in
                showAlreadyPurchased = newValue
            }
            .alert(
                strings.get(id: SharedRes.strings().support_purchase_scheduled_title),
                isPresented: $showScheduled,
                presenting: uiState.scheduledDialogStartTime
            ) { _ in
                Button(strings.get(id: SharedRes.strings().support_purchase_message_dismiss), role: .cancel) {
                    onEvent(SupportUiEventsScheduledDialogDismissed.shared)
                }
            } message: { startTime in
                Text(
                    strings.get(
                        id: SharedRes.strings().support_scheduled_dialog_message,
                        args: [startTime.date.formatted(date: .long, time: .omitted)]
                    )
                )
            }
            .onChange(of: uiState.scheduledDialogStartTime != nil) { _, newValue in
                showScheduled = newValue
            }
    }
}
