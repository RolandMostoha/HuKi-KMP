import Shared
import SwiftUI

@main
struct HukiApp: App {
    init() {
        // Maestro launch arguments
        let args = ProcessInfo.processInfo.arguments
        if let index = args.firstIndex(of: "skipWhatsNew"), index + 1 < args.count {
            AppLaunchConfig.shared.skipWhatsNew = (args[index + 1] as NSString).boolValue
        }
        doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
