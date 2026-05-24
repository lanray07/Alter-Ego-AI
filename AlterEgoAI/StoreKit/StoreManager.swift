import Combine
import Foundation
import StoreKit

@MainActor
final class StoreManager: ObservableObject {
    @Published private(set) var products: [Product] = []
    @Published private(set) var purchasedProductIDs: Set<String> = []
    @Published var purchaseError: String?
    @Published var isLoading = false

    private var updatesTask: Task<Void, Never>?

    var activePlan: SubscriptionPlan {
        if purchasedProductIDs.contains(AppConstants.eliteMonthlyProductID) {
            return .elite
        }
        if purchasedProductIDs.contains(AppConstants.proMonthlyProductID) || purchasedProductIDs.contains(AppConstants.proYearlyProductID) {
            return .pro
        }
        return .free
    }

    deinit {
        updatesTask?.cancel()
    }

    func start() async {
        listenForTransactions()
        await loadProducts()
        await refreshEntitlements()
    }

    func loadProducts() async {
        isLoading = true
        defer { isLoading = false }
        do {
            products = try await Product.products(for: AppConstants.productIDs).sorted { $0.displayName < $1.displayName }
        } catch {
            purchaseError = "Products are not configured yet. Add matching subscriptions in App Store Connect or a StoreKit test configuration."
        }
    }

    func purchase(_ product: Product) async {
        purchaseError = nil
        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                purchasedProductIDs.insert(transaction.productID)
                await transaction.finish()
            case .pending:
                purchaseError = "Purchase is pending approval."
            case .userCancelled:
                break
            @unknown default:
                purchaseError = "Unknown purchase result."
            }
        } catch {
            purchaseError = error.localizedDescription
        }
    }

    func refreshEntitlements() async {
        var activeIDs: Set<String> = []
        for await entitlement in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(entitlement) else { continue }
            if AppConstants.productIDs.contains(transaction.productID) {
                activeIDs.insert(transaction.productID)
            }
        }
        purchasedProductIDs = activeIDs
    }

    func product(for id: String) -> Product? {
        products.first { $0.id == id }
    }

    func displayPrice(for productID: String) -> String {
        product(for: productID)?.displayPrice ?? AppConstants.fallbackPrices[productID] ?? ""
    }

    private func listenForTransactions() {
        guard updatesTask == nil else { return }
        updatesTask = Task {
            for await update in Transaction.updates {
                guard let transaction = try? checkVerified(update) else { continue }
                await refreshEntitlements()
                await transaction.finish()
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let safe):
            return safe
        case .unverified:
            throw StoreError.failedVerification
        }
    }
}

enum StoreError: Error {
    case failedVerification
}
