import SwiftUI
import UIKit

/// Colors of one event card. Mirrors `eventColors` in the Android app so both platforms look alike.
struct EventColors {
    var background: [Color]
    var content: Color
    var number: Color
}

private struct RGB {
    var r, g, b: Double

    init(argb: Int64) {
        r = Double((argb >> 16) & 0xFF) / 255
        g = Double((argb >> 8) & 0xFF) / 255
        b = Double(argb & 0xFF) / 255
    }

    init(_ r: Double, _ g: Double, _ b: Double) {
        self.r = r
        self.g = g
        self.b = b
    }

    static let black = RGB(0, 0, 0)
    static let white = RGB(1, 1, 1)

    var luminance: Double {
        func channel(_ value: Double) -> Double {
            value <= 0.03928 ? value / 12.92 : pow((value + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
    }

    var contrastingText: RGB { luminance > 0.179 ? .black : .white }

    func mixed(with other: RGB, _ fraction: Double) -> RGB {
        RGB(r + (other.r - r) * fraction, g + (other.g - g) * fraction, b + (other.b - b) * fraction)
    }

    func contrast(with other: RGB) -> Double {
        let a = luminance, b = other.luminance
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    var color: Color { Color(red: r, green: g, blue: b) }
    var uiColor: UIColor { UIColor(red: r, green: g, blue: b, alpha: 1) }
}

extension EventSnapshot {
    var colors: EventColors {
        let base = RGB(argb: colorArgb)
        switch background {
        case .system:
            // The number keeps the event color, nudged until it is readable on either system surface.
            func accent(on surface: RGB) -> RGB {
                var accent = base
                for _ in 0..<20 where accent.contrast(with: surface) < 4.5 {
                    accent = accent.mixed(with: surface.contrastingText, 0.12)
                }
                return accent
            }
            let number = UIColor { traits in
                accent(on: traits.userInterfaceStyle == .dark ? RGB(0.11, 0.11, 0.12) : .white).uiColor
            }
            // White in light mode, so the card stands out on the grouped page background.
            let surface = Color(uiColor: .secondarySystemGroupedBackground)
            return EventColors(background: [surface, surface], content: Color(uiColor: .label), number: Color(uiColor: number))
        case .solid:
            let content = base.contrastingText.color
            return EventColors(background: [base.color, base.color], content: content, number: content)
        case .gradient:
            let text = base.contrastingText
            // Move away from the text color so both gradient ends keep readable contrast.
            let end = base.mixed(with: text.luminance == 0 ? .white : .black, 0.28)
            return EventColors(background: [base.color, end.color], content: text.color, number: text.color)
        }
    }
}
