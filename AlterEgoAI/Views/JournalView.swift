import SwiftData
import SwiftUI

struct JournalView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.aiService) private var aiService
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \JournalEntry.createdAt, order: .reverse) private var entries: [JournalEntry]
    @StateObject private var viewModel = JournalViewModel()

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                editor
                entriesSection
            }
            .padding(20)
        }
        .navigationTitle("Journal")
        .background(CinematicBackground())
    }

    private var editor: some View {
        VStack(alignment: .leading, spacing: 14) {
            Picker("Mood", selection: $viewModel.mood) {
                ForEach(Mood.allCases) { mood in
                    Text(mood.title).tag(mood)
                }
            }
            .pickerStyle(.segmented)

            labeledEditor("Daily reflection", text: $viewModel.content, prompt: "What did today reveal about your identity?")
            labeledTextField("Wins", text: $viewModel.wins, prompt: "What did you do right?")
            labeledTextField("Struggles", text: $viewModel.struggles, prompt: "Where did the old pattern show up?")
            labeledTextField("Lesson learned", text: $viewModel.lessonLearned, prompt: "What will you carry into tomorrow?")

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(.red.opacity(0.9))
            }

            Button {
                Task {
                    await viewModel.save(
                        profile: profile,
                        alterEgo: alterEgo,
                        context: modelContext,
                        aiService: aiService
                    )
                }
            } label: {
                HStack {
                    if viewModel.isSaving {
                        ProgressView().tint(.black)
                    }
                    Text(viewModel.isSaving ? "Summarizing" : "Save reflection")
                    Image(systemName: "checkmark")
                }
                .font(.headline)
                .frame(maxWidth: .infinity)
                .padding()
                .background(AppTheme.neonCyan)
                .foregroundStyle(.black)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            }
            .disabled(!viewModel.canSave || viewModel.isSaving)
            .opacity(viewModel.canSave ? 1 : 0.55)
        }
        .cinematicCard()
    }

    private var entriesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Past reflections")
                .font(.headline)
            if entries.isEmpty {
                EmptyStateView(title: "No reflections yet", message: "Write a quick check-in and Alter Ego AI will summarize the pattern.", symbol: "book.closed")
            } else {
                ForEach(entries) { entry in
                    JournalCard(entry: entry)
                }
            }
        }
    }

    private func labeledTextField(_ title: String, text: Binding<String>, prompt: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.subheadline.bold())
            TextField(prompt, text: text, axis: .vertical)
                .lineLimit(1...3)
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 14).fill(.white.opacity(0.08)))
        }
    }

    private func labeledEditor(_ title: String, text: Binding<String>, prompt: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.subheadline.bold())
            ZStack(alignment: .topLeading) {
                if text.wrappedValue.isEmpty {
                    Text(prompt)
                        .foregroundStyle(.white.opacity(0.35))
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                }
                TextEditor(text: text)
                    .frame(minHeight: 118)
                    .padding(8)
                    .scrollContentBackground(.hidden)
                    .background(RoundedRectangle(cornerRadius: 14).fill(.white.opacity(0.08)))
            }
        }
    }
}
