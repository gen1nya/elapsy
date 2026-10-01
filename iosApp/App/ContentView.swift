import SwiftUI
import Shared

private enum AppRoute: Hashable {
    case editor
    case edit(String)
    case settings
}

struct ContentView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.scenePhase) private var scenePhase
    @State private var path: [AppRoute] = []
    @State private var openedLaunchScreen = false

    var body: some View {
        NavigationStack(path: $path) {
            HomeScreen(
                onCreate: { path.append(.editor) },
                onEdit: { path.append(.edit($0)) },
                onSettings: { path.append(.settings) }
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .editor:
                    EventEditorScreen(event: nil, today: store.today)
                case .edit(let id):
                    if let event = store.data.events.first(where: { $0.id == id }) {
                        EventEditorScreen(event: event, today: store.today)
                    } else {
                        Button("event_unavailable") { path.removeAll() }
                            .padding()
                    }
                case .settings:
                    SettingsScreen()
                }
            }
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

    private func openLaunchScreen() {
        #if DEBUG
        guard !openedLaunchScreen else { return }
        openedLaunchScreen = true
        let arguments = ProcessInfo.processInfo.arguments
        guard let index = arguments.firstIndex(of: "-screen"), arguments.indices.contains(index + 1) else { return }
        switch arguments[index + 1] {
        case "editor": path = [.editor]
        case "edit": path = [.edit(store.sortedEvents.first?.id ?? "")]
        case "settings": path = [.settings]
        default: break
        }
        #endif
    }
}
