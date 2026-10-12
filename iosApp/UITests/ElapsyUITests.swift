import XCTest

/// End-to-end checks of the main flows, driven through the real UI on a simulator.
final class ElapsyUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        // English and an empty store make the tests independent of the simulator's state.
        app.launchArguments = ["-resetData", "-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
    }

    func testCreatesAnEvent() {
        XCTAssertTrue(app.staticTexts["Your events will appear here"].waitForExistence(timeout: 5))
        app.buttons["home.createFirst"].tap()

        let save = app.buttons["editor.save"]
        XCTAssertTrue(save.waitForExistence(timeout: 5))
        XCTAssertFalse(save.isEnabled, "nothing to save before a name is entered")

        typeName("Vacation")
        XCTAssertTrue(save.isEnabled)
        save.tap()

        XCTAssertTrue(card("Vacation").waitForExistence(timeout: 5))
        XCTAssertFalse(save.exists, "the editor closes after saving")
    }

    func testEditsAnEvent() {
        createEvent("Vacation")
        card("Vacation").tap()

        let save = app.buttons["editor.save"]
        XCTAssertTrue(save.waitForExistence(timeout: 5))
        XCTAssertFalse(save.isEnabled, "nothing changed yet")

        typeName(" 2026")
        save.tap()

        XCTAssertTrue(card("Vacation 2026").waitForExistence(timeout: 5))
    }

    func testCancelWithChangesAsksBeforeDiscarding() {
        createEvent("Vacation")
        card("Vacation").tap()
        typeName(" changed")

        app.buttons["editor.cancel"].tap()
        XCTAssertTrue(app.buttons["Discard"].waitForExistence(timeout: 5))
        dismissDialogWithoutDiscarding()
        XCTAssertTrue(app.buttons["editor.save"].exists, "the editor stays open")
        XCTAssertFalse(app.buttons["Discard"].exists)

        app.buttons["editor.cancel"].tap()
        app.buttons["Discard"].tap()

        XCTAssertTrue(card("Vacation").waitForExistence(timeout: 5))
        XCTAssertFalse(card("Vacation changed").exists)
    }

    func testCancelWithoutChangesClosesImmediately() {
        createEvent("Vacation")
        card("Vacation").tap()
        XCTAssertTrue(app.buttons["editor.cancel"].waitForExistence(timeout: 5))

        app.buttons["editor.cancel"].tap()

        XCTAssertTrue(waitForDisappearance(of: app.buttons["editor.save"]))
        XCTAssertFalse(app.buttons["Discard"].exists)
    }

    func testSwipeDownClosesAnUntouchedEditor() {
        createEvent("Vacation")
        card("Vacation").tap()
        XCTAssertTrue(app.buttons["editor.save"].waitForExistence(timeout: 5))

        swipeSheetDown()

        XCTAssertTrue(waitForDisappearance(of: app.buttons["editor.save"]))
    }

    func testSwipeDownWithChangesAsksBeforeDiscarding() {
        createEvent("Vacation")
        card("Vacation").tap()
        typeName(" changed\n") // Return hides the keyboard

        swipeSheetDown()

        XCTAssertTrue(app.buttons["Discard"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["editor.save"].exists, "the sheet refuses to close")
        app.buttons["Discard"].tap()
        XCTAssertTrue(waitForDisappearance(of: app.buttons["editor.save"]))
        XCTAssertTrue(card("Vacation").exists)
    }

    func testDeletesAnEvent() {
        createEvent("Vacation")
        card("Vacation").tap()

        let delete = app.buttons["editor.delete"]
        XCTAssertTrue(app.buttons["editor.save"].waitForExistence(timeout: 5))
        while !delete.isHittable { app.swipeUp() }
        delete.tap()
        app.buttons["Delete"].tap()

        XCTAssertTrue(app.staticTexts["Your events will appear here"].waitForExistence(timeout: 5))
    }

    /// Slow by nature: while a context menu is open the app never reports itself idle, and
    /// XCTest waits a full minute before each interaction with it.
    func testLongPressOpensTheMenu() {
        createEvent("Vacation")

        card("Vacation").press(forDuration: 1.2)
        app.buttons["Duplicate"].tap()

        XCTAssertTrue(waitForCount(of: "event.Vacation", toBe: 2))
    }

    func testDragReordersEventsInCustomOrder() {
        createEvent("First")
        createEvent("Second")
        XCTAssertLessThan(card("First").frame.minY, card("Second").frame.minY)

        // Upwards, away from where the card's menu would appear if the press lasted long enough
        // to open it on a slow machine.
        drag(card("Second"), to: card("First").coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2)))

        XCTAssertTrue(waitForOrder(first: "Second", second: "First"))
    }

    func testDroppingBelowTheLastCardMovesTheEventToTheEnd() {
        createEvent("First")
        createEvent("Second")
        createEvent("Third")

        drag(card("First"), to: app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.92)))

        XCTAssertTrue(waitForOrder(first: "Third", second: "First"))
        XCTAssertLessThan(card("Second").frame.minY, card("Third").frame.minY)
    }

    /// Holding until the menu is open and then moving, without lifting the finger, switches from
    /// the menu to dragging, the way the home screen does.
    func testDraggingAfterTheMenuHasOpenedStillReorders() {
        createEvent("First")
        createEvent("Second")
        createEvent("Third")

        // Long enough for the menu to finish appearing even on a slow machine.
        card("First").coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).press(
            forDuration: 2.5,
            thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.92)),
            withVelocity: .slow, thenHoldForDuration: 0.8
        )

        XCTAssertTrue(waitForOrder(first: "Third", second: "First"))
        XCTAssertFalse(app.buttons["Duplicate"].exists, "the menu is gone once the drag starts")
    }

    // MARK: - Helpers

    private func card(_ name: String) -> XCUIElement {
        app.buttons["event.\(name)"].firstMatch
    }

    /// Newer iOS versions show the dialog as a popover without a cancel button; tapping outside
    /// is how it is dismissed there.
    private func dismissDialogWithoutDiscarding() {
        let keepEditing = app.buttons["Keep Editing"]
        if keepEditing.exists {
            keepEditing.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.9)).tap()
        }
        XCTAssertTrue(waitForDisappearance(of: app.buttons["Discard"]))
    }

    private func typeName(_ text: String) {
        let field = app.textFields["editor.name"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText(text)
    }

    private func createEvent(_ name: String) {
        let first = app.buttons["home.createFirst"]
        if first.waitForExistence(timeout: 2) { first.tap() } else { app.buttons["home.add"].tap() }
        typeName(name)
        app.buttons["editor.save"].tap()
        XCTAssertTrue(card(name).waitForExistence(timeout: 5))
    }

    /// Drags the sheet from its navigation bar towards the bottom of the screen.
    private func swipeSheetDown() {
        let start = app.navigationBars.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.95))
        start.press(forDuration: 0.1, thenDragTo: end)
    }

    private func waitForCount(of identifier: String, toBe count: Int) -> Bool {
        let query = app.buttons.matching(identifier: identifier)
        let matches = NSPredicate { _, _ in query.count == count }
        return XCTWaiter().wait(for: [expectation(for: matches, evaluatedWith: nil)], timeout: 5) == .completed
    }

    /// Holds just long enough for the card to lift, then moves: the same long press opens the menu
    /// if the finger stays still.
    private func drag(_ element: XCUIElement, to destination: XCUICoordinate) {
        element.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
            .press(forDuration: 0.6, thenDragTo: destination, withVelocity: .default, thenHoldForDuration: 0.5)
    }

    private func waitForOrder(first: String, second: String) -> Bool {
        let ordered = NSPredicate { _, _ in self.card(first).frame.minY < self.card(second).frame.minY }
        return XCTWaiter().wait(for: [expectation(for: ordered, evaluatedWith: nil)], timeout: 5) == .completed
    }

    private func waitForDisappearance(of element: XCUIElement) -> Bool {
        let gone = NSPredicate(format: "exists == false")
        return XCTWaiter().wait(for: [expectation(for: gone, evaluatedWith: element)], timeout: 5) == .completed
    }
}
