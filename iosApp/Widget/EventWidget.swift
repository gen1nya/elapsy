import Intents
import Shared
import SwiftUI
import WidgetKit

@main
struct ElapsyWidgetBundle: WidgetBundle {
    var body: some Widget {
        EventWidget()
    }
}

struct EventWidget: Widget {
    var body: some WidgetConfiguration {
        // The SiriKit-intent configuration (rather than App Intents) is what makes per-widget event
        // selection work on iOS 16.
        IntentConfiguration(kind: "EventWidget", intent: SelectEventIntent.self, provider: EventTimelineProvider()) { entry in
            EventWidgetView(entry: entry)
        }
        .configurationDisplayName(Text("widget_name"))
        .description(Text("widget_description"))
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

struct EventEntry: TimelineEntry {
    let date: Date
    /// nil when no event is chosen or the chosen one was deleted.
    let snapshot: EventSnapshot?
}

struct EventTimelineProvider: IntentTimelineProvider {
    func placeholder(in context: Context) -> EventEntry {
        EventEntry(date: Date(), snapshot: .sample)
    }

    func getSnapshot(for configuration: SelectEventIntent, in context: Context, completion: @escaping (EventEntry) -> Void) {
        let data = IosBridgeKt.loadAppData(appGroupId: AppGroup.id)
        let entry = entry(for: configuration, at: Date(), data: data)
        // The widget gallery shows this before anything is configured; an empty tile would say nothing.
        completion(context.isPreview && entry.snapshot == nil ? EventEntry(date: Date(), snapshot: .sample) : entry)
    }

    func getTimeline(for configuration: SelectEventIntent, in context: Context, completion: @escaping (Timeline<EventEntry>) -> Void) {
        let data = IosBridgeKt.loadAppData(appGroupId: AppGroup.id)
        let calendar = Calendar.current
        let now = Date()
        // The count only changes at local midnight, so a week of daily entries keeps the widget
        // correct without the app ever running.
        var dates = [now]
        var day = calendar.startOfDay(for: now)
        for _ in 0..<7 {
            guard let next = calendar.date(byAdding: .day, value: 1, to: day) else { break }
            day = next
            dates.append(day)
        }
        completion(Timeline(entries: dates.map { entry(for: configuration, at: $0, data: data) }, policy: .atEnd))
    }

    private func entry(for configuration: SelectEventIntent, at date: Date, data: AppData) -> EventEntry {
        let event: Event?
        if let id = configuration.event?.identifier {
            event = data.events.first { $0.id == id }
        } else {
            // Nothing chosen yet: fall back to the first event in the user's order.
            event = SortingKt.sorted(
                data.events, mode: data.settings.sortMode, today: date.localDate, firstDay: data.settings.firstDay
            ).first
        }
        return EventEntry(date: date, snapshot: event.map { EventSnapshot(event: $0, settings: data.settings, today: date.localDate) })
    }
}

struct EventWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: EventEntry

    var body: some View {
        if let snapshot = entry.snapshot {
            switch family {
            case .accessoryInline:
                Text("\(snapshot.name) · \(snapshot.number) \(snapshot.caption)")
            case .accessoryCircular:
                Text(snapshot.number)
                    .font(.system(.title, design: .rounded).weight(.bold))
                    .minimumScaleFactor(0.4)
                    .lineLimit(1)
                    .padding(4)
                    .widgetBackground(AccessoryWidgetBackground())
            case .accessoryRectangular:
                VStack(alignment: .leading) {
                    Text(snapshot.name).font(.headline).lineLimit(1)
                    Text("\(snapshot.number) \(snapshot.caption)").lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .widgetBackground(Color.clear)
            case .systemMedium:
                EventCardView(snapshot: snapshot, shape: .rectangle)
                    .widgetBackground(EventCardBackground(snapshot: snapshot))
            default:
                EventCardView(snapshot: snapshot, shape: .square)
                    .widgetBackground(EventCardBackground(snapshot: snapshot))
            }
        } else {
            Text("widget_select_event")
                .font(.footnote.weight(.medium))
                .multilineTextAlignment(.center)
                .widgetBackground(Color(uiColor: .secondarySystemBackground))
        }
    }
}

private extension View {
    /// iOS 17 moved widget backgrounds into a container the system can remove; iOS 16 has no such
    /// container and no automatic margins, so the padding is ours there.
    @ViewBuilder
    func widgetBackground<Background: View>(_ background: Background) -> some View {
        if #available(iOSApplicationExtension 17.0, *) {
            containerBackground(for: .widget) { background }
        } else {
            padding(16).frame(maxWidth: .infinity, maxHeight: .infinity).background(background)
        }
    }
}

private extension EventSnapshot {
    static let sample = EventSnapshot(
        name: NSLocalizedString("widget_sample_name", comment: ""),
        icon: "🏝️",
        number: "61",
        caption: String.localizedStringWithFormat(NSLocalizedString("caption_remaining", comment: ""), 61),
        dateLine: "",
        colorArgb: 0xFF566B98,
        background: .gradient
    )
}
