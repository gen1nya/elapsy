import Foundation
import Shared

/// Everything a card or a widget needs to draw one event, already formatted for display.
struct EventSnapshot: Equatable {
    enum Background { case system, solid, gradient }

    var name: String
    var icon: String?
    var number: String
    var caption: String
    var dateLine: String
    var colorArgb: Int64
    var background: Background

    init(event: Event, settings: Settings, today: LocalDate) {
        let count = event.dayCount(today: today, firstDay: settings.firstDay)
        name = event.name
        icon = event.icon
        number = NumberFormatter.localizedString(from: NSNumber(value: count.days), number: .decimal)
        caption = EventFormatter.caption(count)
        dateLine = EventFormatter.dateLine(event, today: today, style: settings.dateFormat)
        colorArgb = event.colorArgb
        switch event.background {
        case .system: background = .system
        case .gradient: background = .gradient
        default: background = .solid
        }
    }

    init(name: String, icon: String?, number: String, caption: String, dateLine: String, colorArgb: Int64, background: Background) {
        self.name = name
        self.icon = icon
        self.number = number
        self.caption = caption
        self.dateLine = dateLine
        self.colorArgb = colorArgb
        self.background = background
    }
}

enum EventFormatter {
    /// The words under the number, e.g. "дня прошло".
    static func caption(_ count: DayCount) -> String {
        let key: String
        switch count.label {
        case .elapsed: key = "caption_elapsed"
        case .remaining: key = "caption_remaining"
        case .ago: key = "caption_ago"
        default: return NSLocalizedString("caption_today", comment: "")
        }
        return String.localizedStringWithFormat(NSLocalizedString(key, comment: ""), Int(count.days))
    }

    static func date(_ date: LocalDate, style: DateFormatStyle) -> String {
        let formatter = DateFormatter()
        switch style {
        case .numeric: formatter.dateStyle = .short
        case .text: formatter.dateStyle = .long
        default: formatter.dateStyle = .medium
        }
        formatter.timeStyle = .none
        return formatter.string(from: date.date)
    }

    /// "01.01.2026 — сегодня" for a running elapsed event, otherwise just the date.
    static func dateLine(_ event: Event, today: LocalDate, style: DateFormatStyle) -> String {
        let text = date(event.date, style: style)
        if event.type == .elapsed && event.date.compareTo(other: today) <= 0 {
            return String(format: NSLocalizedString("date_range_until_today", comment: ""), text)
        }
        return text
    }
}
