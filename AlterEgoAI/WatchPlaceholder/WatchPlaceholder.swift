import SwiftUI

struct WatchMissionPlaceholderView: View {
    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: "applewatch")
                .font(.largeTitle)
                .foregroundStyle(AppTheme.neonCyan)
            Text("Alter Ego AI Watch")
                .font(.headline)
            Text("Quick mission check-ins and streak glance placeholder.")
                .font(.caption)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppTheme.mutedText)
        }
        .padding()
    }
}
