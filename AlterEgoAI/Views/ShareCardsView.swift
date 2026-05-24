import SwiftData
import SwiftUI
import UIKit

struct ShareCardsView: View {
    @Environment(\.aiService) private var aiService
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @StateObject private var viewModel = ShareCardsViewModel()
    @State private var sharePayload: ActivitySharePayload?

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }
    private var shareText: String {
        viewModel.generatedText.isEmpty ? viewModel.selectedTemplate : viewModel.generatedText
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                ShareCardPreview(text: shareText, alterEgo: alterEgo)

                Picker("Template", selection: $viewModel.selectedTemplate) {
                    ForEach(viewModel.templates, id: \.self) { template in
                        Text(template).tag(template)
                    }
                }
                .pickerStyle(.inline)
                .cinematicCard()

                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red.opacity(0.9))
                }

                Button {
                    Task {
                        await viewModel.generate(profile: profile, alterEgo: alterEgo, aiService: aiService)
                    }
                } label: {
                    Label(viewModel.isGenerating ? "Generating" : "Generate share text", systemImage: "wand.and.stars")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(AppTheme.neonCyan)
                        .foregroundStyle(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                }
                .disabled(viewModel.isGenerating)

                ShareLink(item: shareText) {
                    Label("Share text with native sheet", systemImage: "square.and.arrow.up")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.white.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                }

                Button {
                    renderImageShareCard()
                } label: {
                    Label("Share image card", systemImage: "photo")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.white.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                }

                Text("Nothing is shared until you choose Share and confirm in the system share sheet.")
                    .font(.caption)
                    .foregroundStyle(AppTheme.mutedText)
            }
            .padding(20)
        }
        .navigationTitle("Share Cards")
        .background(CinematicBackground())
        .sheet(item: $sharePayload) { payload in
            ActivityShareSheet(items: payload.items)
        }
    }

    @MainActor
    private func renderImageShareCard() {
        let renderer = ImageRenderer(
            content: ShareCardPreview(text: shareText, alterEgo: alterEgo)
                .frame(width: 1080, height: 1350)
                .environment(\.colorScheme, .dark)
        )
        renderer.scale = 1

        if let image = renderer.uiImage {
            sharePayload = ActivitySharePayload(items: [image, shareText])
        } else {
            sharePayload = ActivitySharePayload(items: [shareText])
        }
    }
}
