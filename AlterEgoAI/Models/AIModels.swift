import Foundation

struct GeneratedAlterEgoProfile: Codable {
    var alterEgoName: String
    var identityStatement: String
    var traits: [String]
    var goals: [String]
}

struct GeneratedMission: Codable, Identifiable {
    var id = UUID()
    var title: String
    var category: MissionCategory
    var difficulty: MissionDifficulty
    var xpReward: Int

    enum CodingKeys: String, CodingKey {
        case title
        case category
        case difficulty
        case xpReward
    }
}

struct JournalInsight: Codable {
    var summary: String
    var patternInsight: String
    var nextAction: String
}

struct AIBackendRequest: Codable {
    var module: String
    var currentIdentity: String
    var futureIdentity: String
    var motivationStyle: String
    var goals: [String]
    var journalText: String
    var progressData: [String: String]
}

struct AIBackendResponse: Codable {
    var message: String?
    var missions: [GeneratedMission]?
    var summary: String?
    var insights: [String]?
}
