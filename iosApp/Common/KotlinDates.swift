import Foundation
import Shared

typealias LocalDate = Kotlinx_datetimeLocalDate

extension Kotlinx_datetimeLocalDate {
    /// Midnight of this calendar day in the current time zone.
    var date: Date {
        let components = DateComponents(year: Int(year), month: Int(IosBridgeKt.monthNumber(date: self)), day: Int(day))
        return Calendar.current.date(from: components) ?? Date()
    }
}

extension Date {
    /// The calendar day this moment falls on in the current time zone.
    var localDate: LocalDate {
        let components = Calendar.current.dateComponents([.year, .month, .day], from: self)
        return IosBridgeKt.localDate(
            year: Int32(components.year ?? 1970), month: Int32(components.month ?? 1), day: Int32(components.day ?? 1)
        )
    }
}
