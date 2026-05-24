import SwiftUI

enum AppTheme {
    static let background = Color(red: 0.02, green: 0.02, blue: 0.05)
    static let surface = Color(red: 0.07, green: 0.06, blue: 0.12)
    static let elevated = Color(red: 0.11, green: 0.09, blue: 0.18)
    static let electricBlue = Color(red: 0.15, green: 0.45, blue: 1.0)
    static let neonCyan = Color(red: 0.15, green: 0.95, blue: 1.0)
    static let deepPurple = Color(red: 0.26, green: 0.12, blue: 0.58)
    static let success = Color(red: 0.18, green: 0.9, blue: 0.55)
    static let warning = Color(red: 1.0, green: 0.75, blue: 0.22)
    static let mutedText = Color.white.opacity(0.68)

    static let cinematicGradient = LinearGradient(
        colors: [background, deepPurple.opacity(0.85), Color(red: 0.02, green: 0.08, blue: 0.18)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let accentGradient = LinearGradient(
        colors: [neonCyan, electricBlue, deepPurple],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

struct CinematicBackground: View {
    var body: some View {
        ZStack {
            AppTheme.cinematicGradient
            RadialGradient(
                colors: [AppTheme.neonCyan.opacity(0.22), .clear],
                center: .topTrailing,
                startRadius: 20,
                endRadius: 420
            )
            RadialGradient(
                colors: [AppTheme.deepPurple.opacity(0.35), .clear],
                center: .bottomLeading,
                startRadius: 40,
                endRadius: 520
            )
        }
        .ignoresSafeArea()
    }
}

struct GlowModifier: ViewModifier {
    let color: Color

    func body(content: Content) -> some View {
        content
            .shadow(color: color.opacity(0.35), radius: 18, x: 0, y: 8)
            .shadow(color: color.opacity(0.18), radius: 38, x: 0, y: 18)
    }
}

extension View {
    func glow(_ color: Color = AppTheme.neonCyan) -> some View {
        modifier(GlowModifier(color: color))
    }

    func cinematicCard(radius: CGFloat = 22) -> some View {
        padding(16)
            .background(
                RoundedRectangle(cornerRadius: radius, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: radius, style: .continuous)
                            .stroke(AppTheme.neonCyan.opacity(0.18), lineWidth: 1)
                    )
            )
    }
}
