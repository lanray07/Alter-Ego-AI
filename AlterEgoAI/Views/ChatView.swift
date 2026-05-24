import SwiftData
import SwiftUI

struct ChatView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.aiService) private var aiService
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \Mission.dueDate, order: .reverse) private var missions: [Mission]
    @Query(sort: \ChatMessage.createdAt, order: .forward) private var messages: [ChatMessage]
    @StateObject private var viewModel = ChatViewModel()

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }

    var body: some View {
        VStack(spacing: 0) {
            disclaimer
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(messages) { message in
                            ChatBubbleView(message: message)
                                .id(message.id)
                        }
                        if viewModel.isSending {
                            HStack {
                                ProgressView()
                                Text("Future self is responding")
                                    .font(.caption)
                                    .foregroundStyle(AppTheme.mutedText)
                                Spacer()
                            }
                            .cinematicCard(radius: 16)
                        }
                    }
                    .padding(20)
                }
                .onChange(of: messages.count) {
                    guard let last = messages.last else { return }
                    withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                }
            }
            inputBar
        }
        .navigationTitle("Future Self")
        .background(CinematicBackground())
    }

    private var disclaimer: some View {
        Text("Motivational coaching only. Not therapy, diagnosis, treatment, medical, legal, or financial advice.")
            .font(.caption)
            .foregroundStyle(AppTheme.mutedText)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(.black.opacity(0.2))
    }

    private var inputBar: some View {
        VStack(spacing: 8) {
            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(.red.opacity(0.9))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            HStack(alignment: .bottom, spacing: 10) {
                TextField("Ask your future self for the next move", text: $viewModel.draft, axis: .vertical)
                    .lineLimit(1...4)
                    .padding(12)
                    .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.09)))

                Button {
                    Task {
                        await viewModel.send(
                            profile: profile,
                            alterEgo: alterEgo,
                            missions: Array(missions.prefix(12)),
                            context: modelContext,
                            aiService: aiService
                        )
                    }
                } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.system(size: 34))
                        .foregroundStyle(AppTheme.neonCyan)
                }
                .disabled(viewModel.draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.isSending)
            }
        }
        .padding(16)
        .background(.ultraThinMaterial)
    }
}
