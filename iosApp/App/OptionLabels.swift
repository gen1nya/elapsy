import SwiftUI
import Shared

extension SortMode {
    var label: LocalizedStringKey {
        switch self {
        case .newest: return "sort_newest"
        case .oldest: return "sort_oldest"
        case .name: return "sort_name"
        case .days: return "sort_days"
        case .nearest: return "sort_nearest"
        default: return "sort_custom"
        }
    }
}

extension CountType {
    var label: LocalizedStringKey {
        switch self {
        case .remaining: return "type_remaining"
        case .ago: return "type_ago"
        default: return "type_elapsed"
        }
    }

    var dateLabel: LocalizedStringKey {
        switch self {
        case .remaining: return "end_date"
        case .ago: return "event_date"
        default: return "start_date"
        }
    }
}

extension Shared.BackgroundStyle {
    var label: LocalizedStringKey {
        switch self {
        case .system: return "background_system"
        case .gradient: return "background_gradient"
        default: return "background_solid"
        }
    }
}

extension ThemeMode {
    var label: LocalizedStringKey {
        switch self {
        case .light: return "theme_light"
        case .dark: return "theme_dark"
        default: return "theme_system"
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .light: return .light
        case .dark: return .dark
        default: return nil
        }
    }
}

extension FirstDay {
    var label: LocalizedStringKey { self == .day1 ? "day_one" : "day_zero" }
}

extension DateFormatStyle {
    var label: LocalizedStringKey {
        switch self {
        case .numeric: return "date_format_numeric"
        case .text: return "date_format_text"
        default: return "date_format_system"
        }
    }
}
