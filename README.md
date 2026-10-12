# Elapsy

A day counter for iOS and Android: create an event, pick a date, and the number of days passed
(or left) is always in front of you, mainly through home screen widgets. No account, no
subscription, no tracking.

## Layout

| Directory | What it is |
|---|---|
| `core/` | Kotlin Multiplatform: events, day counting, sorting, JSON storage, import/export. Exported to iOS as the static framework `Shared`. |
| `androidApp/` | Android app: Jetpack Compose, Glance home screen widget. |
| `iosApp/` | iOS app: SwiftUI, WidgetKit widget, intents extension for widget configuration, UI tests. The Xcode project is generated from `project.yml` with [XcodeGen](https://github.com/yonaskolb/XcodeGen). |
| `scripts/` | Build helpers that run the iOS side on a Mac over SSH. |
| `docs/` | Backlog, screenshots, icon candidates, store listing and privacy policy drafts. |

## Building

Android and the shared core build on any machine with a JDK and the Android SDK:

```sh
./gradlew :core:jvmTest                 # unit tests of the shared core
./gradlew :androidApp:assembleDebug     # debug APK
./gradlew :androidApp:bundleRelease     # Play bundle; signed when ~/.elapsy/keystore.properties exists
```

iOS needs a Mac with Xcode and XcodeGen. The scripts sync this directory to the Mac named in
`scripts/mac.sh` and run there:

```sh
scripts/ios-build.sh      # simulator build
scripts/ios-test.sh       # UI tests on the simulator
scripts/ios-testflight.sh # archive and upload to App Store Connect
```

On the Mac itself: `cd iosApp && xcodegen generate && open Elapsy.xcodeproj`.

## Data

Everything lives in one JSON document (`AppData` in `core`), stored in the app's private files on
Android and in the App Group container on iOS so the widgets can read it. The same document is the
export format.
