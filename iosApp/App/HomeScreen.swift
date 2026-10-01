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
        let events = store.sortedEvents
        return ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                    Button { onEdit(event.id) } label: {
                        EventCardSurface(snapshot: store.snapshot(of: event))
                            .frame(height: 158)
                    }
                    .buttonStyle(.plain)
                    .contextMenu {
                        Button { onEdit(event.id) } label: {
                            Label("edit_event", systemImage: "pencil")
                        }
                        Button { _ = store.repository.duplicate(id: event.id) } label: {
                            Label("duplicate_event", systemImage: "plus.square.on.square")
                        }
                        if store.data.settings.sortMode == .custom {
                            Button { move(event.id, offset: -1) } label: {
                                Label("move_up", systemImage: "arrow.up")
                            }
                            .disabled(index == 0)
                            Button { move(event.id, offset: 1) } label: {
                                Label("move_down", systemImage: "arrow.down")
                            }
                            .disabled(index == events.count - 1)
                        }
                        Button(role: .destructive) { deletingEvent = event } label: {
                            Label("delete_event", systemImage: "trash")
                        }
                    }
                }
            }
            .padding(16)
            .frame(maxWidth: 640)
            .frame(maxWidth: .infinity)
        }
    }

    private func move(_ id: String, offset: Int) {
        var ids = store.sortedEvents.map(\.id)
        guard store.data.settings.sortMode == .custom,
              let index = ids.firstIndex(of: id), ids.indices.contains(index + offset) else { return }
        ids.swapAt(index, index + offset)
        store.repository.reorder(orderedIds: ids)
    }
}
