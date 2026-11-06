import Shared
import SwiftUI

struct MainSceneView: View {
    // iOS keeps SceneStorage across both an OS kill and a swipe-away in the app switcher
    @SceneStorage("huki.main_session") private var session = ""
    @Environment(\.scenePhase) private var scenePhase
    @State private var viewModelHolder = MainViewModelHolder()

    var body: some View {
        let viewModel = viewModelHolder.resolve(session: session)
        MainView(viewModel: viewModel)
            .onChange(of: scenePhase) { _, phase in
                guard phase != .active else { return }
                session = viewModel.sessionSnapshot()
            }
    }
}

// Creates the view model in the first body pass, when SceneStorage is already readable, and keeps it after that
private final class MainViewModelHolder {
    private var viewModel: MainViewModel?

    func resolve(session: String) -> MainViewModel {
        if let viewModel { return viewModel }
        let created = KoinViewModelProvider.shared.getMainViewModel(session: session)
        viewModel = created
        return created
    }
}
