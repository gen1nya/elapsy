import SwiftUI
import Shared

struct HomeScreen: View {
    @EnvironmentObject private var store: AppStore
    @State private var deletingEvent: Event?

    let onCreate: () -> Void
    let onEdit: (String) -> Void
    let onSettings: () -> Void

    var body: some View {
        Group {
            if store.sortedEvents.isEmpty {
                emptyState
            } else {
                eventList
            }
        }
        .background(Color(uiColor: .systemGroupedBackground))
        .navigationTitle("app_name")
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button(action: onCreate) { Label("add_event", systemImage: "plus") }
                    .accessibilityIdentifier("home.add")
                Menu {
                    Picker("sort_events", selection: Binding(
                        get: { store.data.settings.sortMode },
                        set: { store.updateSettings(sortMode: $0) }
                    )) {
                        ForEach(SortMode.entries, id: \.self) { mode in
                            Text(mode.label).tag(mode)
                        }
                    }
                } label: {
                    Label("sort_events", systemImage: "arrow.up.arrow.down")
                }
                Button(action: onSettings) { Label("settings", systemImage: "gearshape") }
                    .accessibilityIdentifier("home.settings")
            }
        }
        .deleteEventConfirmation(event: $deletingEvent) { event in
            store.repository.delete(id: event.id)
        }
    }

    private var emptyState: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Image(systemName: "calendar")
                        .font(.system(size: 36, weight: .medium))
                        .foregroundStyle(Color.accentColor)
                        .frame(width: 88, height: 88)
                        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 28))
                        .accessibilityHidden(true)
                    Text("empty_title")
                        .font(.title2.weight(.semibold))
                    Text("empty_description")
                        .foregroundStyle(.secondary)
                    Button("create_first_event", action: onCreate)
                        .accessibilityIdentifier("home.createFirst")
                        .buttonStyle(.borderedProminent)
                        .controlSize(.large)
                }
                .multilineTextAlignment(.center)
                .padding(32)
                .frame(maxWidth: 420)
                .frame(maxWidth: .infinity, minHeight: geometry.size.height)
            }
        }
    }

    private var eventList: some View {
        EventCollectionView(
            items: store.sortedEvents.map { .init(id: $0.id, name: $0.name, snapshot: store.snapshot(of: $0)) },
            canReorder: store.data.settings.sortMode == .custom,
            onOpen: onEdit,
            onDuplicate: { _ = store.repository.duplicate(id: $0) },
            onDelete: { id in deletingEvent = store.data.events.first { $0.id == id } },
            onReorder: { store.repository.reorder(orderedIds: $0) }
        )
        // The list runs under the bars so the large title collapses while it scrolls.
        .ignoresSafeArea()
    }
}
