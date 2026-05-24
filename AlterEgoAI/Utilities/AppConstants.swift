import Foundation

enum AppConstants {
    static let appName = "Alter Ego AI"
    static let tagline = "Become the person you were supposed to be."
    static let viralHook = "Your future self is watching."

    static let coachingDisclaimer = """
    Alter Ego AI is a general wellness, habit-building, motivation, and lifestyle app. AI coaching is informational and motivational only. It is not medical advice, mental health care, therapy, diagnosis, crisis counseling, financial advice, legal advice, or treatment. For medical, mental health, legal, financial, or safety concerns, seek qualified professional support.
    """

    static let aiSystemPrompt = """
    You are Alter Ego AI, a motivational future-self coach for general wellness and habit-building. Help users build discipline, confidence, focus, and healthier routines through practical daily actions. Use inspiring, cinematic, identity-based language. Do not provide medical advice, therapy, mental health diagnosis, crisis counselling, financial guarantees, or harmful shaming. Encourage users to seek qualified professional support for medical, mental health, legal, or financial concerns.
    """

    static let backendEndpoint = URL(string: "https://YOUR_BACKEND_URL.com/alter-ego-ai")!

    static let proMonthlyProductID = "com.alteregoai.pro.monthly"
    static let proYearlyProductID = "com.alteregoai.pro.yearly"
    static let eliteMonthlyProductID = "com.alteregoai.elite.monthly"

    static let productIDs: Set<String> = [
        proMonthlyProductID,
        proYearlyProductID,
        eliteMonthlyProductID
    ]

    static let fallbackPrices: [String: String] = [
        proMonthlyProductID: "£9.99",
        proYearlyProductID: "£79.99",
        eliteMonthlyProductID: "£19.99"
    ]
}
