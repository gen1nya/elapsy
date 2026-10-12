import SwiftUI
import UIKit

/// The list of event cards on the home screen.
///
/// It is a UIKit collection view because that is where the system combines what the cards need:
/// a long press opens a menu, moving the finger while holding turns it into drag-to-reorder, and a
/// touch highlights the card, the way icons behave on the home screen.
struct EventCollectionView: UIViewRepresentable {
    struct Item: Equatable {
        let id: String
        let name: String
        let snapshot: EventSnapshot
    }

    let items: [Item]
    /// Dragging is only meaningful in the user's own order.
    let canReorder: Bool
    let onOpen: (String) -> Void
    let onDuplicate: (String) -> Void
    let onDelete: (String) -> Void
    let onReorder: ([String]) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> UICollectionView {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: Self.layout())
        collectionView.backgroundColor = .clear
        collectionView.alwaysBounceVertical = true
        collectionView.delegate = context.coordinator
        collectionView.dragDelegate = context.coordinator
        collectionView.dropDelegate = context.coordinator
        collectionView.dragInteractionEnabled = true
        context.coordinator.attach(to: collectionView)
        return collectionView
    }

    func updateUIView(_ collectionView: UICollectionView, context: Context) {
        context.coordinator.parent = self
        context.coordinator.apply(items)
    }

    private static func layout() -> UICollectionViewLayout {
        let size = NSCollectionLayoutSize(widthDimension: .fractionalWidth(1), heightDimension: .absolute(158))
        let group = NSCollectionLayoutGroup.vertical(layoutSize: size, subitems: [NSCollectionLayoutItem(layoutSize: size)])
        let section = NSCollectionLayoutSection(group: group)
        section.interGroupSpacing = 16
        section.contentInsets = NSDirectionalEdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)
        return UICollectionViewCompositionalLayout(section: section)
    }

    final class Coordinator: NSObject, UICollectionViewDelegate, UICollectionViewDragDelegate, UICollectionViewDropDelegate {
        var parent: EventCollectionView
        private var items: [String: Item] = [:]
        private var dataSource: UICollectionViewDiffableDataSource<Int, String>!

        init(_ parent: EventCollectionView) {
            self.parent = parent
        }

        func attach(to collectionView: UICollectionView) {
            let registration = UICollectionView.CellRegistration<UICollectionViewCell, String> { [unowned self] cell, _, id in
                guard let item = items[id] else { return }
                cell.contentConfiguration = UIHostingConfiguration {
                    EventCardSurface(snapshot: item.snapshot)
                        .accessibilityAddTraits(.isButton)
                        .accessibilityIdentifier("event.\(item.name)")
                }
                .margins(.all, 0)
                cell.configurationUpdateHandler = { cell, state in
                    UIView.animate(
                        withDuration: 0.3, delay: 0, usingSpringWithDamping: 0.7, initialSpringVelocity: 0,
                        options: [.beginFromCurrentState, .allowUserInteraction]
                    ) {
                        cell.transform = state.isHighlighted ? CGAffineTransform(scaleX: 0.96, y: 0.96) : .identity
                    }
                }
            }
            dataSource = UICollectionViewDiffableDataSource(collectionView: collectionView) { collectionView, indexPath, id in
                collectionView.dequeueConfiguredReusableCell(using: registration, for: indexPath, item: id)
            }
        }

        func apply(_ newItems: [Item]) {
            let old = items
            items = Dictionary(newItems.map { ($0.id, $0) }, uniquingKeysWith: { first, _ in first })
            var snapshot = NSDiffableDataSourceSnapshot<Int, String>()
            snapshot.appendSections([0])
            snapshot.appendItems(newItems.map(\.id))
            snapshot.reconfigureItems(newItems.filter { old[$0.id] != nil && old[$0.id] != $0 }.map(\.id))
            dataSource.apply(snapshot, animatingDifferences: !old.isEmpty)
        }

        // MARK: Tap and menu

        func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
            collectionView.deselectItem(at: indexPath, animated: false)
            if let id = dataSource.itemIdentifier(for: indexPath) { parent.onOpen(id) }
        }

        func collectionView(
            _ collectionView: UICollectionView, contextMenuConfigurationForItemsAt indexPaths: [IndexPath], point: CGPoint
        ) -> UIContextMenuConfiguration? {
            guard let indexPath = indexPaths.first, let id = dataSource.itemIdentifier(for: indexPath) else { return nil }
            return UIContextMenuConfiguration(actionProvider: { [unowned self] _ in
                UIMenu(children: [
                    UIAction(title: NSLocalizedString("edit_event", comment: ""), image: UIImage(systemName: "pencil")) { _ in
                        self.parent.onOpen(id)
                    },
                    UIAction(title: NSLocalizedString("duplicate_event", comment: ""), image: UIImage(systemName: "plus.square.on.square")) { _ in
                        self.parent.onDuplicate(id)
                    },
                    UIAction(title: NSLocalizedString("delete_event", comment: ""), image: UIImage(systemName: "trash"), attributes: .destructive) { _ in
                        self.parent.onDelete(id)
                    },
                ])
            })
        }

        func collectionView(
            _ collectionView: UICollectionView, contextMenuConfiguration configuration: UIContextMenuConfiguration,
            highlightPreviewForItemAt indexPath: IndexPath
        ) -> UITargetedPreview? {
            collectionView.cellForItem(at: indexPath).map { UITargetedPreview(view: $0, parameters: previewParameters(for: $0)) }
        }

        func collectionView(
            _ collectionView: UICollectionView, contextMenuConfiguration configuration: UIContextMenuConfiguration,
            dismissalPreviewForItemAt indexPath: IndexPath
        ) -> UITargetedPreview? {
            collectionView.cellForItem(at: indexPath).map { UITargetedPreview(view: $0, parameters: previewParameters(for: $0)) }
        }

        /// The lifted card keeps its rounded shape instead of sitting on an opaque rectangle.
        private func previewParameters(for cell: UICollectionViewCell) -> UIDragPreviewParameters {
            let parameters = UIDragPreviewParameters()
            parameters.backgroundColor = .clear
            parameters.visiblePath = UIBezierPath(roundedRect: cell.bounds, cornerRadius: 24)
            return parameters
        }

        // MARK: Drag to reorder

        func collectionView(_ collectionView: UICollectionView, itemsForBeginning session: UIDragSession, at indexPath: IndexPath) -> [UIDragItem] {
            guard parent.canReorder, let id = dataSource.itemIdentifier(for: indexPath) else { return [] }
            let item = UIDragItem(itemProvider: NSItemProvider(object: id as NSString))
            item.localObject = id
            return [item]
        }

        func collectionView(_ collectionView: UICollectionView, dragPreviewParametersForItemAt indexPath: IndexPath) -> UIDragPreviewParameters? {
            collectionView.cellForItem(at: indexPath).map(previewParameters)
        }

        func collectionView(_ collectionView: UICollectionView, dragSessionIsRestrictedToDraggingApplication session: UIDragSession) -> Bool {
            true
        }

        func collectionView(
            _ collectionView: UICollectionView, dropSessionDidUpdate session: UIDropSession,
            withDestinationIndexPath destinationIndexPath: IndexPath?
        ) -> UICollectionViewDropProposal {
            guard session.localDragSession != nil else { return UICollectionViewDropProposal(operation: .forbidden) }
            return UICollectionViewDropProposal(operation: .move, intent: .insertAtDestinationIndexPath)
        }

        func collectionView(_ collectionView: UICollectionView, performDropWith coordinator: UICollectionViewDropCoordinator) {
            guard let dropped = coordinator.items.first, let id = dropped.dragItem.localObject as? String else { return }
            var ids = dataSource.snapshot().itemIdentifiers
            guard let from = ids.firstIndex(of: id) else { return }
            // Letting go below the last card has no destination; it means "put it last".
            let to = min(coordinator.destinationIndexPath?.item ?? ids.count - 1, ids.count - 1)
            ids.remove(at: from)
            ids.insert(id, at: to)

            var snapshot = NSDiffableDataSourceSnapshot<Int, String>()
            snapshot.appendSections([0])
            snapshot.appendItems(ids)
            dataSource.apply(snapshot, animatingDifferences: true)
            coordinator.drop(dropped.dragItem, toItemAt: IndexPath(item: to, section: 0))
            parent.onReorder(ids)
        }

        func collectionView(_ collectionView: UICollectionView, dropPreviewParametersForItemAt indexPath: IndexPath) -> UIDragPreviewParameters? {
            collectionView.cellForItem(at: indexPath).map(previewParameters)
        }
    }
}
