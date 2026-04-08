import UIKit
import SwiftUI
import ComposeApp // <-- TADY zkontroluj, že se to jmenuje jako tvůj sdílený modul

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Tady si iOS volá tvůj Kotlin kód
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}



