import Intents
import Shared

/// Supplies the list of events shown when the user edits a widget.
final class IntentHandler: INExtension, SelectEventIntentHandling {
    func provideEventOptionsCollection(
        for intent: SelectEventIntent,
        with completion: @escaping (INObjectCollection<WidgetEvent>?, Error?) -> Void
    ) {
        completion(INObjectCollection(items: items()), nil)
    }

    /// A freshly added widget shows the first event instead of an empty tile.
    func defaultEvent(for intent: SelectEventIntent) -> WidgetEvent? {
        items().first
    }

    private func items() -> [WidgetEvent] {
        let data = IosBridgeKt.loadAppData(appGroupId: AppGroup.id)
        let events = SortingKt.sorted(
            data.events, mode: data.settings.sortMode, today: Date().localDate, firstDay: data.settings.firstDay
        )
        return events.map { event in
            WidgetEvent(identifier: event.id, display: [event.icon, event.name].compactMap { $0 }.joined(separator: " "))
        }
    }

    override func handler(for intent: INIntent) -> Any {
        self
    }
}
