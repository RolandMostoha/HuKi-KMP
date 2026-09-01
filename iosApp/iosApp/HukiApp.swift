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
        #if DEBUG
        if let index = args.firstIndex(of: "importGpx"), index + 1 < args.count,
           (args[index + 1] as NSString).boolValue {
            AppLaunchConfig.shared.importGpxPath = Bundle.main.path(
                forResource: "gpx_test_smoke",
                ofType: "gpx"
            )
        }
        #endif
        doInitKoin(analyticsService: IosAnalyticsService(), crashlyticsService: IosCrashlyticsService())
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
