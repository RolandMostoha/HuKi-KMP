import SwiftUI

/// Lays out its subviews in rows, wrapping to a new row when the next one doesn't fit the proposed width.
struct FlowLayout: Layout {
    var spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let rows = rows(for: subviews, maxWidth: proposal.width ?? .infinity)
        let width = rows.map(\.width).max() ?? 0
        let height = rows.map(\.height).reduce(0, +) + spacing * CGFloat(max(rows.count - 1, 0))
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var rowY = bounds.minY
        for row in rows(for: subviews, maxWidth: bounds.width) {
            var itemX = bounds.minX
            for item in row.items {
                subviews[item.index].place(
                    at: CGPoint(x: itemX, y: rowY + (row.height - item.size.height) / 2),
                    proposal: ProposedViewSize(item.size)
                )
                itemX += item.size.width + spacing
            }
            rowY += row.height + spacing
        }
    }

    private struct Item {
        let index: Int
        let size: CGSize
    }

    private struct Row {
        var items: [Item] = []
        var width: CGFloat = 0
        var height: CGFloat = 0
    }

    private func rows(for subviews: Subviews, maxWidth: CGFloat) -> [Row] {
        var rows: [Row] = []
        var current = Row()
        for index in subviews.indices {
            let idealSize = subviews[index].sizeThatFits(.unspecified)
            // A subview wider than a whole row is capped, so its text truncates instead of overflowing
            let size = idealSize.width > maxWidth
                ? subviews[index].sizeThatFits(ProposedViewSize(width: maxWidth, height: nil))
                : idealSize
            if !current.items.isEmpty && current.width + spacing + size.width > maxWidth {
                rows.append(current)
                current = Row()
            }
            current.width += (current.items.isEmpty ? 0 : spacing) + size.width
            current.height = max(current.height, size.height)
            current.items.append(Item(index: index, size: size))
        }
        if !current.items.isEmpty {
            rows.append(current)
        }
        return rows
    }
}
