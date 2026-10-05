import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("Shri Madhwa Yuvaka Sangha")
                .font(.title)
                .bold()
            Text("Food Count Portal")
                .font(.headline)
                .foregroundColor(.secondary)
        }
        .padding()
    }
}
