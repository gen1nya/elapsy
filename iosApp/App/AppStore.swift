import Foundation
import Shared
import WidgetKit

/// Bridges the shared Kotlin repository into SwiftUI: publishes its data and exposes it for mutations.
@MainActor
final class AppStore: ObservableObject {
    let repository: EventRepository
    @Published private(set) var data: AppData
    @Published private(set) var today: LocalDate = Date().localDate
    private var subscription: Subscription?

    init() {
        repository = IosBridgeKt.createRepository(appGroupId: AppGroup.id) {
            WidgetCenter.shared.reloadAllTimelines()
        }
        data = repository.data.value as? AppData ?? AppData(schemaVersion: 1, settings: Settings(firstDay: .day0, theme: .system, dateFormat: .system, sortMode: .custom), events: [])
        subscription = repository.observe { [weak self] data in
            self?.data = data
        }
    }

    /// Events in the order the user chose.
    var sortedEvents: [Event] {
        SortingKt.sorted(data.events, mode: data.settings.sortMode, today: today, firstDay: data.settings.firstDay)
    }

    func snapshot(of event: Event) -> EventSnapshot {
        EventSnapshot(event: event, settings: data.settings, today: today)
    }

    func updateSettings(firstDay: FirstDay? = nil, theme: ThemeMode? = nil, dateFormat: DateFormatStyle? = nil, sortMode: SortMode? = nil) {
        repository.updateSettings { settings in
            settings.doCopy(
                firstDay: firstDay ?? settings.firstDay,
                theme: theme ?? settings.theme,
                dateFormat: dateFormat ?? settings.dateFormat,
                sortMode: sortMode ?? settings.sortMode
            )
        }
    }

    /// Call when the app becomes active so counts never show yesterday's value.
    func refreshToday() {
        let now = Date().localDate
        if now != today { today = now }
    }
}
