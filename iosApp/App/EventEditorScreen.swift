import SwiftUI
import Shared

struct EventEditorScreen: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.dismiss) private var dismiss
    @FocusState private var nameFocused: Bool
    @State private var name: String
    @State private var type: CountType
    @State private var date: Date
    @State private var color: Int64
    @State private var background: Shared.BackgroundStyle
    @State private var icon: String?
    @State private var shape: EventCardView.Shape = .rectangle
    @State private var showingEmojiPicker = false
    @State private var deletingEvent: Event?

    let event: Event?
    private let maxNameLength = Int(Event.companion.MAX_NAME_LENGTH)

    init(event: Event?, today: LocalDate) {
        self.event = event
        _name = State(initialValue: event?.name ?? "")
        _type = State(initialValue: event?.type ?? .elapsed)
        _date = State(initialValue: (event?.date ?? today).date)
        _color = State(initialValue: event?.colorArgb ?? EventPalette.colors[0])
        _background = State(initialValue: event?.background ?? .solid)
        _icon = State(initialValue: event?.icon)
    }

    var body: some View {
        VStack(spacing: 0) {
            EventCardSurface(snapshot: preview, shape: shape)
                .frame(maxWidth: shape == .square ? 158 : 480)
                .frame(height: 158)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 8)
            Form {
                Section {
                    Picker("preview_shape", selection: $shape) {
                        Text("shape_square").tag(EventCardView.Shape.square)
                        Text("shape_rectangle").tag(EventCardView.Shape.rectangle)
                    }
                    .pickerStyle(.segmented)
                } header: {
                    Text("preview_shape")
                }
                nameSection
                Section {
                    Picker("count_type", selection: $type) {
                        ForEach(CountType.entries, id: \.self) { type in
                            Text(type.label).tag(type)
                        }
                    }
                    DatePicker(type.dateLabel, selection: $date, displayedComponents: .date)
                        .datePickerStyle(.compact)
                }
                Section("color") {
                    EventColorPicker(selection: $color)
                }
                Section {
                    Picker("background", selection: $background) {
                        ForEach(Shared.BackgroundStyle.entries, id: \.self) { background in
                            Text(background.label).tag(background)
                        }
                    }
                    Button {
                        nameFocused = false
                        showingEmojiPicker = true
                    } label: {
                        HStack {
                            Text("icon").foregroundStyle(.primary)
                            Spacer()
                            if let icon {
                                Text(icon)
                            } else {
                                Text("no_icon").foregroundStyle(.secondary)
                            }
                            Image(systemName: "chevron.right")
                                .font(.footnote.weight(.semibold))
                                .foregroundStyle(.tertiary)
                        }
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }
                Section {
                    Button(action: save) {
                        Text(event == nil ? LocalizedStringKey("create_event") : LocalizedStringKey("save_changes"))
                            .frame(maxWidth: .infinity, minHeight: 32)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(trimmedName.isEmpty)
                    .listRowBackground(Color.clear)
                    .listRowInsets(EdgeInsets())
                    if let event {
                        Button("delete_event", role: .destructive) {
                            nameFocused = false
                            deletingEvent = event
                        }
                        .frame(maxWidth: .infinity, minHeight: 44)
                        .listRowBackground(Color.clear)
                    }
                }
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .background(Color(uiColor: .systemGroupedBackground))
        .navigationTitle(event == nil ? LocalizedStringKey("new_event") : LocalizedStringKey("edit_event_title"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("done") { nameFocused = false }
            }
        }
        .sheet(isPresented: $showingEmojiPicker) {
            EventEmojiPicker(selection: $icon)
        }
        .deleteEventConfirmation(event: $deletingEvent) { event in
            store.repository.delete(id: event.id)
            dismiss()
        }
    }

    private var nameSection: some View {
        Section {
            TextField("event_name_example", text: $name)
                .accessibilityLabel(Text("event_name"))
                .textInputAutocapitalization(.sentences)
                .submitLabel(.done)
                .focused($nameFocused)
                .onSubmit { nameFocused = false }
                .onChange(of: name) { value in
                    if value.count > maxNameLength { name = String(value.prefix(maxNameLength)) }
                }
        } header: {
            Text("event_name")
        } footer: {
            Text(localized("name_length", name.count, maxNameLength))
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
    }

    private var trimmedName: String {
        String(name.trimmingCharacters(in: .whitespacesAndNewlines).prefix(maxNameLength))
    }

    private var draft: Event {
        if let event {
            return event.doCopy(
                id: event.id, name: trimmedName, type: type, date: date.localDate,
                colorArgb: color, background: background, icon: icon,
                createdAtEpochMs: event.createdAtEpochMs, sortOrder: event.sortOrder
            )
        }
        return Event(
            id: "", name: trimmedName, type: type, date: date.localDate,
            colorArgb: color, background: background, icon: icon,
            createdAtEpochMs: 0, sortOrder: 0
        )
    }

    private var preview: EventSnapshot {
        var snapshot = store.snapshot(of: draft)
        if trimmedName.isEmpty { snapshot.name = NSLocalizedString("event_name_placeholder", comment: "") }
        return snapshot
    }

    private func save() {
        guard !trimmedName.isEmpty else { return }
        nameFocused = false
        let draft = draft
        if event == nil {
            _ = store.repository.create(
                name: draft.name, type: draft.type, date: draft.date,
                colorArgb: draft.colorArgb, background: draft.background, icon: draft.icon
            )
        } else {
            store.repository.update(event: draft)
        }
        dismiss()
    }
}
