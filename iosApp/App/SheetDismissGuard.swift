import SwiftUI
import UIKit

extension View {
    /// For the root view of a sheet: while `isBlocked` is true the swipe-down gesture does not
    /// close the sheet, and each refused attempt is reported through `onAttempt`.
    func sheetDismissGuard(isBlocked: Bool, onAttempt: @escaping () -> Void) -> some View {
        interactiveDismissDisabled(isBlocked)
            .background(DismissAttemptObserver(onAttempt: onAttempt))
    }
}

/// SwiftUI can refuse an interactive dismissal but does not say when one was attempted; UIKit does,
/// through the sheet's presentation controller delegate.
private struct DismissAttemptObserver: UIViewControllerRepresentable {
    let onAttempt: () -> Void

    func makeUIViewController(context: Context) -> ObserverController {
        ObserverController()
    }

    func updateUIViewController(_ controller: ObserverController, context: Context) {
        controller.proxy.onAttempt = onAttempt
    }

    final class ObserverController: UIViewController {
        let proxy = PresentationDelegateProxy()

        override func viewDidAppear(_ animated: Bool) {
            super.viewDidAppear(animated)
            var root: UIViewController = self
            while let parent = root.parent { root = parent }
            guard let presentation = root.presentationController, presentation.delegate !== proxy else { return }
            // SwiftUI already owns this delegate and relies on it to learn that the sheet was
            // dismissed, so it is wrapped rather than replaced.
            proxy.original = presentation.delegate
            presentation.delegate = proxy
        }
    }

    final class PresentationDelegateProxy: NSObject, UIAdaptivePresentationControllerDelegate {
        weak var original: UIAdaptivePresentationControllerDelegate?
        var onAttempt: () -> Void = {}

        func presentationControllerDidAttemptToDismiss(_ presentationController: UIPresentationController) {
            original?.presentationControllerDidAttemptToDismiss?(presentationController)
            onAttempt()
        }

        override func responds(to selector: Selector!) -> Bool {
            super.responds(to: selector) || (original?.responds(to: selector) ?? false)
        }

        override func forwardingTarget(for selector: Selector!) -> Any? {
            original
        }
    }
}
