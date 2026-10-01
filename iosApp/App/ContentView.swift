import SwiftUI
import Shared

private enum AppRoute: Hashable {
    case settings
}

/// What the editor sheet is editing.
private enum EditorTarget: Identifiable {
    case new
    case existing(Event)

    var id: String {
        switch self {
        case .new: return ""
        case .existing(let event): return event.id
        }
    }

    var event: Event? {
        if case .existing(let event) = self { return event }
        return nil
    }
}

struct ContentView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.scenePhase) private var scenePhase
    @State private var path: [AppRoute] = []
    @State private var editor: EditorTarget?
    @State private var openedLaunchScreen = false

    var body: some View {
        NavigationStack(path: $path) {
            HomeScreen(
                onCreate: { editor = .new },
                onEdit: edit,
                onSettings: { path.append(.settings) }
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .settings:
                    SettingsScreen()
                }
            }
        }
        // Creating and editing is a self-contained task, so it is a modal sheet rather than a
        // pushed screen: Cancel and Save are native there.
        .sheet(item: $editor) { target in
            NavigationStack {
                EventEditorScreen(event: target.event, today: store.today)
            }
            .environmentObject(store)
        }
        .preferredColorScheme(store.data.settings.theme.colorScheme)
        .onChange(of: scenePhase) { phase in
            if phase == .active { store.refreshToday() }
        }
        .onReceive(NotificationCenter.default.publisher(for: .NSCalendarDayChanged)) { _ in
            store.refreshToday()
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.significantTimeChangeNotification)) { _ in
            store.refreshToday()
        }
        .task {
            store.refreshToday()
            openLaunchScreen()
        }
    }

    private func edit(_ id: String) {
        if let event = store.data.events.first(where: { $0.id == id }) { editor = .existing(event) }
    }

    private func openLaunchScreen() {
        #if DEBUG
        guard !openedLaunchScreen else { return }
        openedLaunchScreen = true
        let arguments = ProcessInfo.processInfo.arguments
        guard let index = arguments.firstIndex(of: "-screen"), arguments.indices.contains(index + 1) else { return }
        switch arguments[index + 1] {
        case "editor": editor = .new
        case "edit": if let id = store.sortedEvents.first?.id { edit(id) }
        case "settings": path = [.settings]
        default: break
        }
        #endif
    }
}
