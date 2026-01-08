import Shared
import SwiftUI

@main
struct HukiApp: App {
    init() {
        KoinUtilsKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
