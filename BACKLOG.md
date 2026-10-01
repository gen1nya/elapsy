# Elapsy backlog

Agreed on 2026-10-01. Items move up or down as the brother (who wrote the original spec) and
the first testers give feedback.

## 1.0 — before the first store release

- [x] iOS: event editor as a modal sheet (Cancel / Save in the bar, swipe down closes it when
      nothing changed, asks to discard when something did). Android keeps the full-screen editor.
- [x] Drag to reorder events in the custom sort order, instead of "move up / move down" in the menu.
- [x] iOS UI tests that cover the main flows (create, edit, discard, delete, swipe actions, reorder).
- [ ] Decide the minimum iOS version. Xcode 27 offers no iOS 16 simulator, so 16 cannot be tested
      here at all; the oldest runtime Xcode lists is iOS 17. Then run the UI tests on that runtime.
- [x] Android: a real widget preview in the launcher's widget picker (it shows the app icon now).
- [ ] Final app icon (candidates in `docs/icon-candidates/`; waiting for the choice).
- [ ] Privacy policy: text is drafted in `docs/store/privacy-policy.md`; it still needs a contact
      address and a public URL (both stores require one).
- [ ] Store listings in Russian and English: drafted in `docs/store/listing.md`, to be reviewed.
- [ ] Store screenshots for iPhone and Android.
- [ ] Google Play: closed test with 12 testers for 14 days (required for new personal accounts).

## 1.1 — widgets, the part that sets the app apart

- [ ] Progress for countdowns: a ring or a bar from a start date to the target date.
- [ ] "1 year 2 months 3 days" breakdown as an alternative to a plain day count.
- [ ] Backgrounds: user photos, tint that changes through the day.
- [ ] A list widget with several events.
- [ ] A ticking timer on the last day before an event.
- [ ] Quick-create sheet: name, type and date only; colour, background and emoji behind "More".

## 1.2 — reasons to come back

- [ ] Milestone notifications: 100 days, a year, "tomorrow is the day".
- [ ] Repeating events: a birthday moves to the next year by itself.
- [ ] Resetting a counter with history (current streak and best streak).
- [ ] Share an event card as an image.

## Later

- [ ] Sync between devices (iCloud).
- [ ] Apple Watch, Live Activity for the last day.
- [ ] Siri / Shortcuts ("how many days until the vacation").
- [ ] Monetisation, if ever: a one-time purchase for photo backgrounds, or a tip jar. No subscription.

## Technical debt

- [ ] CI: core tests and both builds on every push.
- [ ] Storage errors from the Kotlin core do not reach the user on iOS.
- [ ] Move the iOS widget configuration to App Intents once iOS 16 support is dropped.
- [ ] The Mac build host address is hard-coded in `scripts/mac.sh` and changes with DHCP.
