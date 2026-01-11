import Shared
import SwiftUI

@main
struct HukiApp: App {
    init() {
        IosKoinModuleKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
