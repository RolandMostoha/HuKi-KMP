import Foundation
import Shared
import KMPNativeCoroutinesAsync

@MainActor
final class MainViewModelWrapper: ObservableObject {
    private var task: Task<Void, Never>?

    let viewModel: MainViewModel

    @Published
    var uiState: MainUiState = MainUiState.Companion().Default

    init() {
        self.viewModel = KoinViewModelProvider.shared.getMainViewModel()
    }

    func startObserving() async {
        task = Task {
            do {
                let sequence = asyncSequence(for: viewModel.uiStateFlow)
                for try await uiSate in sequence {
                    print("Main: UiSate=\(uiSate)")
                    self.uiState = uiSate
                }
            } catch {
                print("Main: failed UiState observing with error: \(error)")
            }
        }
    }

    deinit {
        task?.cancel()
        viewModel.onCleared()
    }
}
