import SwiftUI
import WidgetKit

struct AlterEgoWidgetEntry: TimelineEntry {
    let date: Date
    let alterEgoName: String
    let level: Int
    let streak: Int
}

struct AlterEgoWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> AlterEgoWidgetEntry {
        AlterEgoWidgetEntry(date: Date(), alterEgoName: "Apex Self", level: 10, streak: 7)
    }

    func getSnapshot(in context: Context, completion: @escaping (AlterEgoWidgetEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<AlterEgoWidgetEntry>) -> Void) {
        completion(Timeline(entries: [placeholder(in: context)], policy: .after(Date().addingTimeInterval(3600))))
    }
}

struct AlterEgoWidgetPlaceholderView: View {
    let entry: AlterEgoWidgetEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(entry.alterEgoName)
                .font(.headline)
            Text("Level \(entry.level)")
                .font(.caption.bold())
            Text("\(entry.streak)-day streak")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .containerBackground(.black, for: .widget)
    }
}
