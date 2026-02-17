import Shared
import SwiftUI

@main
struct HukiApp: App {
    init() {
        doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
