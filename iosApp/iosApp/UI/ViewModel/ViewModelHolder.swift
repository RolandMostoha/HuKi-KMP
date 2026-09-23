import Shared
import SwiftUI

/// Holds a KMP ViewModel for the view's lifetime via `@StateObject`.
/// Handles lifecycle events, deinit (`onCleared`, `viewModelScope`).
final class ViewModelHolder<VM: AnyObject>: ObservableObject {
    let viewModel: VM
    private let owner = IosViewModelStoreOwner()

    init(_ create: (IosViewModelStoreOwner) -> VM) {
        viewModel = create(owner)
    }

    deinit {
        owner.clear()
    }
}
