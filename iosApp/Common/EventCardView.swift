import SwiftUI

/// Draws an event the way its home screen widget looks. Used by the widget itself and by the app.
struct EventCardView: View {
    enum Shape { case square, rectangle }

    let snapshot: EventSnapshot
    var shape: Shape = .rectangle

    var body: some View {
        let colors = snapshot.colors
        Group {
            switch shape {
            case .square:
                VStack(alignment: .leading, spacing: 2) {
                    if let icon = snapshot.icon { Text(icon).font(.title3) }
                    Spacer(minLength: 0)
                    number(colors, size: 46)
                    Text(snapshot.caption).font(.footnote).lineLimit(1)
                    Text(snapshot.name).font(.subheadline.weight(.semibold)).lineLimit(1)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
            case .rectangle:
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        if let icon = snapshot.icon { Text(icon).font(.title2) }
                        Text(snapshot.name).font(.headline).lineLimit(2)
                        Text(snapshot.dateLine).font(.caption).lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    Rectangle().fill(colors.content.opacity(0.18)).frame(width: 1)
                    VStack(spacing: 0) {
                        number(colors, size: 44)
                        Text(snapshot.caption).font(.footnote).lineLimit(1)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .foregroundStyle(colors.content)
    }

    private func number(_ colors: EventColors, size: CGFloat) -> some View {
        Text(snapshot.number)
            .font(.system(size: size, weight: .bold, design: .rounded))
            .foregroundStyle(colors.number)
            .lineLimit(1)
            .minimumScaleFactor(0.4)
    }
}

struct EventCardBackground: View {
    let snapshot: EventSnapshot

    var body: some View {
        LinearGradient(colors: snapshot.colors.background, startPoint: .topLeading, endPoint: .bottomTrailing)
    }
}
