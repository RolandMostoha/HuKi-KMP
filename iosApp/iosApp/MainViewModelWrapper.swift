import Foundation
import KMPNativeCoroutinesAsync
import Shared

@MainActor
final class MainViewModelWrapper: ObservableObject {
    private var uiStateTask: Task<Void, Never>?
    private var uiEffectTask: Task<Void, Never>?

    let viewModel: MainViewModel

    @Published
    var uiState: MainUiState = MainUiState.Companion().Default

    init() {
        self.viewModel = KoinViewModelProvider.shared.getMainViewModel()
    }

    func observeUiState() async -> MainUiState {
        uiStateTask = Task {
            do {
                let sequence = asyncSequence(for: viewModel.uiStateFlow)
                for try await uiSate in sequence {
                    self.uiState = uiSate
                }
            } catch {
                print("MainViewModelWrapper: failed UiState observing with error: \(error)")
            }
        }
        return uiState
    }

    func observeUiEffect(onEffect: @escaping (MainUiEffects) -> Void) async {
        uiEffectTask = Task {
            do {
                let sequence = asyncSequence(for: viewModel.uiEffect)
                for try await uiEffect in sequence {
                    onEffect(uiEffect)
                }
            } catch {
                print("MainViewModelWrapper: failed UiEffect observing with error: \(error)")
            }
        }
    }

    deinit {
        uiStateTask?.cancel()
        uiEffectTask?.cancel()
        viewModel.onCleared()
    }
}
