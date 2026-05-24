import SwiftUI
import UIKit

struct AlterEgoCard: View {
    let alterEgo: AlterEgoProfile
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 8) {
                    Text(alterEgo.alterEgoName)
                        .font(.system(.title, design: .rounded, weight: .bold))
                    Text(subtitle)
                        .font(.subheadline)
                        .foregroundStyle(AppTheme.mutedText)
                }
                Spacer()
                AvatarGlyph(stage: alterEgo.avatarStage)
            }

            Text(alterEgo.identityStatement)
                .font(.callout)
                .foregroundStyle(.white.opacity(0.82))
                .fixedSize(horizontal: false, vertical: true)

            HStack {
                LevelBadge(level: alterEgo.level, title: XPSystem.title(for: alterEgo.level))
                Spacer()
                Text("\(alterEgo.totalXP) XP")
                    .font(.headline)
                    .foregroundStyle(AppTheme.neonCyan)
            }
        }
        .cinematicCard()
        .glow(AppTheme.deepPurple)
    }
}

struct AvatarGlyph: View {
    let stage: AvatarStage

    var body: some View {
        ZStack {
            Circle()
                .fill(AppTheme.accentGradient)
                .frame(width: 72, height: 72)
                .blur(radius: 0.2)
            Image(systemName: symbol)
                .font(.system(size: 30, weight: .semibold))
                .foregroundStyle(.black.opacity(0.82))
        }
        .accessibilityLabel(stage.title)
    }

    private var symbol: String {
        switch stage {
        case .drifter:
            return "moon"
        case .awakening:
            return "sunrise"
        case .focused:
            return "scope"
        case .disciplined:
            return "shield"
        case .ascendant:
            return "sparkles"
        case .apexSelf:
            return "crown"
        }
    }
}

struct MissionCard: View {
    let mission: Mission
    let onComplete: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: mission.category.symbolName)
                .font(.title3)
                .foregroundStyle(mission.completed ? AppTheme.success : AppTheme.neonCyan)
                .frame(width: 34, height: 34)
                .background(Circle().fill(.white.opacity(0.08)))

            VStack(alignment: .leading, spacing: 6) {
                Text(mission.title)
                    .font(.headline)
                    .foregroundStyle(mission.completed ? .white.opacity(0.6) : .white)
                    .strikethrough(mission.completed)
                HStack(spacing: 8) {
                    Text(mission.category.title)
                    Text(mission.difficulty.title)
                    Text("+\(mission.xpReward) XP")
                }
                .font(.caption)
                .foregroundStyle(AppTheme.mutedText)
            }

            Spacer()

            Button(action: onComplete) {
                Image(systemName: mission.completed ? "checkmark.seal.fill" : "circle")
                    .font(.title2)
                    .foregroundStyle(mission.completed ? AppTheme.success : .white.opacity(0.54))
            }
            .buttonStyle(.plain)
            .accessibilityLabel(mission.completed ? "Mission completed" : "Complete mission")
        }
        .cinematicCard(radius: 18)
    }
}

struct XPProgressRing: View {
    let progress: Double
    let label: String

    var body: some View {
        ZStack {
            Circle()
                .stroke(.white.opacity(0.12), lineWidth: 12)
            Circle()
                .trim(from: 0, to: min(1, max(0, progress)))
                .stroke(AppTheme.accentGradient, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .animation(.spring(response: 0.8, dampingFraction: 0.82), value: progress)
            VStack(spacing: 4) {
                Text("\(Int(progress * 100))%")
                    .font(.title3.bold())
                Text(label)
                    .font(.caption2)
                    .foregroundStyle(AppTheme.mutedText)
            }
        }
        .frame(width: 112, height: 112)
        .accessibilityLabel("\(label) \(Int(progress * 100)) percent")
    }
}

struct LevelBadge: View {
    let level: Int
    let title: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "bolt.fill")
            Text("Level \(level)")
            Text(title)
                .foregroundStyle(.white.opacity(0.72))
        }
        .font(.caption.bold())
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Capsule().fill(AppTheme.neonCyan.opacity(0.14)))
        .overlay(Capsule().stroke(AppTheme.neonCyan.opacity(0.28), lineWidth: 1))
    }
}

struct StreakCard: View {
    let streak: Int
    let disciplineScore: Int

    var body: some View {
        HStack(spacing: 14) {
            metric(title: "Streak", value: "\(streak)", symbol: "flame.fill", color: AppTheme.warning)
            Divider().background(.white.opacity(0.18))
            metric(title: "Discipline", value: "\(disciplineScore)", symbol: "target", color: AppTheme.neonCyan)
        }
        .frame(maxWidth: .infinity)
        .cinematicCard(radius: 18)
    }

    private func metric(title: String, value: String, symbol: String, color: Color) -> some View {
        HStack(spacing: 10) {
            Image(systemName: symbol)
                .foregroundStyle(color)
            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.title2.bold())
                Text(title)
                    .font(.caption)
                    .foregroundStyle(AppTheme.mutedText)
            }
            Spacer()
        }
    }
}

struct JournalCard: View {
    let entry: JournalEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(entry.mood.title)
                    .font(.caption.bold())
                    .foregroundStyle(AppTheme.neonCyan)
                Spacer()
                Text(entry.createdAt, style: .date)
                    .font(.caption)
                    .foregroundStyle(AppTheme.mutedText)
            }
            Text(entry.aiSummary.isEmpty ? entry.content : entry.aiSummary)
                .font(.callout)
                .foregroundStyle(.white.opacity(0.84))
        }
        .cinematicCard(radius: 18)
    }
}

struct InsightCard: View {
    let title: String
    let value: String
    let symbol: String
    var tint: Color = AppTheme.neonCyan

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Image(systemName: symbol)
                .foregroundStyle(tint)
            Text(value)
                .font(.title2.bold())
            Text(title)
                .font(.caption)
                .foregroundStyle(AppTheme.mutedText)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cinematicCard(radius: 18)
    }
}

struct ShareCardPreview: View {
    let text: String
    let alterEgo: AlterEgoProfile?

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack {
                Text("Alter Ego AI")
                    .font(.caption.bold())
                    .foregroundStyle(AppTheme.neonCyan)
                Spacer()
                Text(AppConstants.viralHook)
                    .font(.caption2)
                    .foregroundStyle(.white.opacity(0.64))
            }

            Text(text.isEmpty ? "Day 7 of becoming my alter ego" : text)
                .font(.system(.title2, design: .rounded, weight: .bold))
                .fixedSize(horizontal: false, vertical: true)

            if let alterEgo {
                HStack {
                    AvatarGlyph(stage: alterEgo.avatarStage)
                        .scaleEffect(0.72)
                        .frame(width: 54, height: 54)
                    VStack(alignment: .leading) {
                        Text(alterEgo.alterEgoName)
                            .font(.headline)
                        Text("Level \(alterEgo.level) \(alterEgo.avatarStage.title)")
                            .font(.caption)
                            .foregroundStyle(AppTheme.mutedText)
                    }
                }
            }
        }
        .padding(22)
        .frame(maxWidth: .infinity, minHeight: 260, alignment: .topLeading)
        .background(
            RoundedRectangle(cornerRadius: 28, style: .continuous)
                .fill(AppTheme.cinematicGradient)
                .overlay(
                    RoundedRectangle(cornerRadius: 28, style: .continuous)
                        .stroke(AppTheme.neonCyan.opacity(0.32), lineWidth: 1.2)
                )
        )
        .glow(AppTheme.neonCyan)
    }
}

struct UpgradeBanner: View {
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: "sparkles")
                .font(.title3)
                .foregroundStyle(.black)
                .frame(width: 42, height: 42)
                .background(Circle().fill(AppTheme.neonCyan))
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.headline)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(AppTheme.mutedText)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundStyle(.white.opacity(0.54))
        }
        .cinematicCard(radius: 18)
    }
}

struct ChatBubbleView: View {
    let message: ChatMessage

    var body: some View {
        HStack {
            if message.role == .user { Spacer(minLength: 42) }
            Text(message.content)
                .font(.callout)
                .padding(14)
                .background(
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .fill(message.role == .user ? AppTheme.electricBlue.opacity(0.78) : AppTheme.elevated)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .stroke(message.role == .user ? Color.clear : AppTheme.neonCyan.opacity(0.16), lineWidth: 1)
                )
            if message.role != .user { Spacer(minLength: 42) }
        }
    }
}

struct EmptyStateView: View {
    let title: String
    let message: String
    let symbol: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: symbol)
                .font(.largeTitle)
                .foregroundStyle(AppTheme.neonCyan)
            Text(title)
                .font(.headline)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(AppTheme.mutedText)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .cinematicCard(radius: 18)
    }
}

struct ActivitySharePayload: Identifiable {
    let id = UUID()
    let items: [Any]
}

struct ActivityShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
