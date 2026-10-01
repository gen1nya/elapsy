import SwiftUI

enum EventPalette {
    static let colors: [Int64] = [
        0xFFB9C7F4, 0xFFD7C2ED, 0xFFF1BFD2, 0xFFF2BEAE,
        0xFFF2D1A0, 0xFFEAE1A2, 0xFFC8D9A9, 0xFFAFD8C6,
        0xFFA8D7DF, 0xFFACCBE7, 0xFF596B9B, 0xFF596C66,
    ]

    static func color(_ argb: Int64) -> Color {
        Color(
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255
        )
    }
}

struct EventColorPicker: View {
    @Binding var selection: Int64

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 44), spacing: 4)], spacing: 4) {
            ForEach(Array(EventPalette.colors.enumerated()), id: \.element) { index, argb in
                Button { selection = argb } label: {
                    Circle()
                        .fill(EventPalette.color(argb))
                        .overlay(Circle().strokeBorder(Color.primary.opacity(0.12), lineWidth: 1))
                        .overlay {
                            if selection == argb {
                                Image(systemName: "checkmark")
                                    .font(.body.weight(.semibold))
                                    .foregroundStyle(index < 10 ? Color.black : Color.white)
                            }
                        }
                        .frame(width: 36, height: 36)
                        .frame(maxWidth: .infinity, minHeight: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(LocalizedStringKey("color_name_\(index)")))
                .accessibilityAddTraits(selection == argb ? .isSelected : [])
            }
        }
        .padding(.vertical, 4)
    }
}

struct EventEmojiPicker: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var selection: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    Button { select(nil) } label: {
                        HStack {
                            Text("no_icon")
                            Spacer()
                            if selection == nil { Image(systemName: "checkmark") }
                        }
                        .padding()
                        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 12))
                    }
                    .accessibilityAddTraits(selection == nil ? .isSelected : [])
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 48))], spacing: 12) {
                        ForEach(0..<40) { index in
                            let emoji = NSLocalizedString("emoji_preset_\(index)", comment: "")
                            Button { select(emoji) } label: {
                                Text(emoji)
                                    .font(.system(size: 28))
                                    .frame(maxWidth: .infinity, minHeight: 48)
                                    .background(
                                        selection == emoji ? Color.accentColor.opacity(0.16) : Color.clear,
                                        in: RoundedRectangle(cornerRadius: 12)
                                    )
                                    .overlay {
                                        if selection == emoji {
                                            RoundedRectangle(cornerRadius: 12)
                                                .strokeBorder(Color.accentColor, lineWidth: 2)
                                        }
                                    }
                                    .contentShape(Rectangle())
                            }
                            .accessibilityLabel(Text(LocalizedStringKey("emoji_name_\(index)")))
                            .accessibilityAddTraits(selection == emoji ? .isSelected : [])
                        }
                    }
                }
                .buttonStyle(.plain)
                .padding(20)
            }
            .background(Color(uiColor: .systemGroupedBackground))
            .navigationTitle("icon")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("cancel") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }

    private func select(_ emoji: String?) {
        selection = emoji
        dismiss()
    }
}
