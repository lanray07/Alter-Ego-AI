import SwiftData
import SwiftUI

struct OnboardingView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.aiService) private var aiService
    @StateObject private var viewModel = OnboardingViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                header
                identityFields
                goalsSection
                preferencesSection
                disclaimerSection
                submitButton
            }
            .padding(20)
            .padding(.bottom, 36)
        }
        .scrollDismissesKeyboard(.interactively)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(AppConstants.viralHook.uppercased())
                .font(.caption.bold())
                .foregroundStyle(AppTheme.neonCyan)
            Text(AppConstants.appName)
                .font(.system(size: 42, weight: .black, design: .rounded))
                .minimumScaleFactor(0.7)
            Text(AppConstants.tagline)
                .font(.title3.weight(.semibold))
                .foregroundStyle(.white.opacity(0.78))
        }
        .padding(.top, 28)
    }

    private var identityFields: some View {
        VStack(alignment: .leading, spacing: 14) {
            labeledTextField("Name", text: $viewModel.name, prompt: "What should your future self call you?")
            labeledEditor("Current identity", text: $viewModel.currentIdentity, prompt: "Describe the version of you that exists today.")
            labeledEditor("Future identity", text: $viewModel.futureIdentity, prompt: "Describe who you are becoming.")
        }
    }

    private var goalsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Top 3 goals")
                    .font(.headline)
                Spacer()
                Text("\(viewModel.selectedGoals.count)/3")
                    .font(.caption.bold())
                    .foregroundStyle(AppTheme.neonCyan)
            }

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 148), spacing: 10)], spacing: 10) {
                ForEach(GoalCategory.allCases) { goal in
                    Button {
                        viewModel.toggleGoal(goal)
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: goal.symbolName)
                            Text(goal.title)
                                .font(.callout.weight(.semibold))
                                .lineLimit(1)
                                .minimumScaleFactor(0.78)
                            Spacer()
                        }
                        .padding(12)
                        .background(
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .fill(viewModel.selectedGoals.contains(goal) ? AppTheme.neonCyan.opacity(0.2) : .white.opacity(0.07))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .stroke(viewModel.selectedGoals.contains(goal) ? AppTheme.neonCyan : .white.opacity(0.1), lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .cinematicCard()
    }

    private var preferencesSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Picker("Motivation style", selection: $viewModel.motivationStyle) {
                ForEach(MotivationStyle.allCases) { style in
                    Text(style.title).tag(style)
                }
            }
            .pickerStyle(.segmented)

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Daily availability")
                    Spacer()
                    Text("\(viewModel.dailyAvailability) min")
                        .foregroundStyle(AppTheme.neonCyan)
                }
                Slider(
                    value: Binding(
                        get: { Double(viewModel.dailyAvailability) },
                        set: { viewModel.dailyAvailability = Int($0) }
                    ),
                    in: 10...120,
                    step: 5
                )
            }

            Toggle("Daily mission reminders", isOn: $viewModel.notificationPreference)
        }
        .cinematicCard()
    }

    private var disclaimerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label("AI coaching disclaimer", systemImage: "info.circle")
                .font(.headline)
            Text(AppConstants.coachingDisclaimer)
                .font(.footnote)
                .foregroundStyle(AppTheme.mutedText)
                .fixedSize(horizontal: false, vertical: true)
        }
        .cinematicCard(radius: 18)
    }

    private var submitButton: some View {
        VStack(spacing: 10) {
            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundStyle(.red.opacity(0.9))
            }

            Button {
                Task {
                    await viewModel.complete(context: modelContext, aiService: aiService)
                }
            } label: {
                HStack {
                    if viewModel.isGenerating {
                        ProgressView()
                            .tint(.black)
                    }
                    Text(viewModel.isGenerating ? "Generating future self" : "Create my Alter Ego")
                        .font(.headline)
                    Image(systemName: "arrow.right")
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(AppTheme.neonCyan)
                .foregroundStyle(.black)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            }
            .disabled(!viewModel.canSubmit || viewModel.isGenerating)
            .opacity(viewModel.canSubmit ? 1 : 0.55)
        }
    }

    private func labeledTextField(_ title: String, text: Binding<String>, prompt: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
            TextField(prompt, text: text)
                .textInputAutocapitalization(.words)
                .padding(14)
                .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.08)))
        }
    }

    private func labeledEditor(_ title: String, text: Binding<String>, prompt: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
            ZStack(alignment: .topLeading) {
                if text.wrappedValue.isEmpty {
                    Text(prompt)
                        .foregroundStyle(.white.opacity(0.35))
                        .padding(.horizontal, 18)
                        .padding(.vertical, 16)
                }
                TextEditor(text: text)
                    .frame(minHeight: 92)
                    .padding(10)
                    .scrollContentBackground(.hidden)
                    .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.08)))
            }
        }
    }
}
