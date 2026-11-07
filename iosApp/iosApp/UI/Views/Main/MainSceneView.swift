import Shared
import SwiftUI

struct MainSceneView: View {
    // AppStorage writes to UserDefaults right away, SceneStorage only on backgrounding, which a foreground kill skips
    @AppStorage("huki.main_session") private var session = ""
    @Environment(\.scenePhase) private var scenePhase
    @State private var viewModelHolder = MainViewModelHolder()

    var body: some View {
        let viewModel = viewModelHolder.resolve(session: session)
        MainView(viewModel: viewModel)
            .onChange(of: scenePhase) { _, phase in
                guard phase != .active else { return }
                session = viewModel.sessionSnapshot()
            }
            .task {
                for await snapshot in viewModel.sessionSnapshots {
                    session = snapshot
                }
            }
    }
}

// Creates the view model in the first body pass with the saved session, and keeps it after that
private final class MainViewModelHolder {
    private var viewModel: MainViewModel?

    func resolve(session: String) -> MainViewModel {
        if let viewModel { return viewModel }
        let created = KoinViewModelProvider.shared.getMainViewModel(session: session)
        viewModel = created
        return created
    }
}
