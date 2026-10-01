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
        let customOrder = store.data.settings.sortMode == .custom
        // A List rather than a stack of cards, because it brings long-press drag reordering.
        return List {
            ForEach(store.sortedEvents, id: \.id) { event in
                Button { onEdit(event.id) } label: {
                    EventCardSurface(snapshot: store.snapshot(of: event))
                        .frame(height: 158)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("event.\(event.name)")
                // Swipe actions rather than a context menu: a long press is reserved for dragging.
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button { deletingEvent = event } label: {
                        Label("delete", systemImage: "trash")
                    }
                    .tint(.red)
                    Button { _ = store.repository.duplicate(id: event.id) } label: {
                        Label("duplicate_event", systemImage: "plus.square.on.square")
                    }
                    .tint(.indigo)
                }
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
            }
            .onMove(perform: customOrder ? move : nil)
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }

    private func move(from source: IndexSet, to destination: Int) {
        var ids = store.sortedEvents.map(\.id)
        ids.move(fromOffsets: source, toOffset: destination)
        store.repository.reorder(orderedIds: ids)
    }
}
