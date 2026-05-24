import StoreKit
import SwiftUI

struct PaywallView: View {
    @EnvironmentObject private var storeManager: StoreManager

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                header
                planCard(
                    title: "Free",
                    price: "Included",
                    subtitle: "Start the identity loop.",
                    features: ["3 daily missions", "Basic habit tracking", "Limited AI messages", "7-day timeline"],
                    productID: nil
                )
                planCard(
                    title: "Pro",
                    price: "\(storeManager.displayPrice(for: AppConstants.proMonthlyProductID)) / month",
                    subtitle: "Build momentum without limits.",
                    features: ["Unlimited AI future-self messages", "Custom mission generation", "Advanced insights", "Transformation timeline", "Viral share cards", "Widgets", "Premium avatar themes"],
                    productID: AppConstants.proMonthlyProductID
                )
                planCard(
                    title: "Pro Yearly",
                    price: "\(storeManager.displayPrice(for: AppConstants.proYearlyProductID)) / year",
                    subtitle: "Commit to the long arc.",
                    features: ["All Pro features", "Yearly placeholder pricing", "Best value configuration slot"],
                    productID: AppConstants.proYearlyProductID
                )
                planCard(
                    title: "Elite",
                    price: "\(storeManager.displayPrice(for: AppConstants.eliteMonthlyProductID)) / month",
                    subtitle: "Cinematic identity coaching.",
                    features: ["Advanced AI personalities", "Cinematic identity cards", "Deep weekly reviews", "Future-self voice placeholder", "Apple Watch placeholder", "Premium themes"],
                    productID: AppConstants.eliteMonthlyProductID
                )

                if let error = storeManager.purchaseError {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red.opacity(0.9))
                }

                Button {
                    Task { await storeManager.refreshEntitlements() }
                } label: {
                    Label("Restore purchases", systemImage: "arrow.clockwise")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)

                Text("Subscriptions use StoreKit 2. Configure matching product identifiers in App Store Connect or a StoreKit test configuration before production testing.")
                    .font(.caption)
                    .foregroundStyle(AppTheme.mutedText)
            }
            .padding(20)
        }
        .navigationTitle("Upgrade")
        .background(CinematicBackground())
        .task {
            await storeManager.loadProducts()
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Become without ceilings")
                .font(.system(.largeTitle, design: .rounded, weight: .black))
            Text("Premium unlocks deeper coaching and transformation artifacts while keeping purchases compliant with StoreKit and In-App Purchase.")
                .foregroundStyle(AppTheme.mutedText)
        }
    }

    private func planCard(title: String, price: String, subtitle: String, features: [String], productID: String?) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.title2.bold())
                    Text(subtitle)
                        .font(.subheadline)
                        .foregroundStyle(AppTheme.mutedText)
                }
                Spacer()
                Text(price)
                    .font(.headline)
                    .foregroundStyle(AppTheme.neonCyan)
                    .multilineTextAlignment(.trailing)
            }

            ForEach(features, id: \.self) { feature in
                Label(feature, systemImage: "checkmark.circle.fill")
                    .font(.callout)
                    .foregroundStyle(.white.opacity(0.84))
            }

            if let productID {
                Button {
                    guard let product = storeManager.product(for: productID) else {
                        storeManager.purchaseError = "Product \(productID) is not loaded yet."
                        return
                    }
                    Task { await storeManager.purchase(product) }
                } label: {
                    Text(storeManager.product(for: productID) == nil ? "Configure product" : "Choose \(title)")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(AppTheme.neonCyan)
                        .foregroundStyle(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }
            }
        }
        .cinematicCard()
    }
}
