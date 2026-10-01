import SwiftUI
import Shared

func localized(_ key: String, _ arguments: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: arguments)
}

struct EventCardSurface: View {
    let snapshot: EventSnapshot
    var shape: EventCardView.Shape = .rectangle

    var body: some View {
        EventCardView(snapshot: snapshot, shape: shape)
            .padding(16)
            .background(EventCardBackground(snapshot: snapshot))
            .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(localized("event_description", snapshot.name, snapshot.number, snapshot.caption, snapshot.dateLine))
    }
}

private struct DeleteEventConfirmation: ViewModifier {
    @Binding var event: Event?
    let onDelete: (Event) -> Void

    func body(content: Content) -> some View {
        content.confirmationDialog(
            "delete_event_title",
            isPresented: Binding(get: { event != nil }, set: { if !$0 { event = nil } }),
            titleVisibility: .visible,
            presenting: event
        ) { event in
            Button("delete", role: .destructive) { onDelete(event) }
            Button("cancel", role: .cancel) {}
        } message: { event in
            Text(localized("delete_event_message", event.name))
        }
    }
}

extension View {
    func deleteEventConfirmation(event: Binding<Event?>, onDelete: @escaping (Event) -> Void) -> some View {
        modifier(DeleteEventConfirmation(event: event, onDelete: onDelete))
    }
}
