import Shared
import SwiftUI
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        return true
    }
}

@main
struct HukiApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        // Maestro launch arguments
        let args = ProcessInfo.processInfo.arguments
        if let index = args.firstIndex(of: "skipWhatsNew"), index + 1 < args.count {
            AppLaunchConfig.shared.skipWhatsNew = (args[index + 1] as NSString).boolValue
        }
        doInitKoin(analyticsService: IosAnalyticsService())
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
