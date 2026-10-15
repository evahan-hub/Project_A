import ArtlyUi
import SwiftUI

@main
struct iOSApp: App {
    init() {
        IosModuleKt.startApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // The shared UI draws behind the system bars and handles their insets itself.
                .ignoresSafeArea()
        }
    }
}

/// Shows the shared Compose UI.
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
