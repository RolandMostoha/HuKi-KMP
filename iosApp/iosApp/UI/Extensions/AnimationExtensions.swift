import Shared
import SwiftUI

extension Animation {
    static var stateChange: Animation {
        .smooth(duration: AnimationConstants.shared.STATE_CHANGE_ANIM_DURATION_S)
    }

    static var toggle: Animation {
        .smooth(duration: AnimationConstants.shared.TOGGLE_ANIM_DURATION_S)
    }

    static var selection: Animation {
        .easeInOut(duration: AnimationConstants.shared.SELECTION_ANIM_DURATION_S)
    }

    static var expand: Animation {
        .easeInOut(duration: AnimationConstants.shared.EXPAND_ANIM_DURATION_S)
    }

    static var menuExpand: Animation {
        .smooth(duration: AnimationConstants.shared.MENU_EXPAND_ANIM_DURATION_S)
    }

    static var fabReveal: Animation {
        .smooth(duration: AnimationConstants.shared.FAB_REVEAL_ANIM_DURATION_S)
    }

    static var pressFeedback: Animation {
        .easeOut(duration: AnimationConstants.shared.PRESS_FEEDBACK_ANIM_DURATION_S)
    }

    static var colorSchemeChange: Animation {
        .easeInOut(duration: AnimationConstants.shared.COLOR_SCHEME_ANIM_DURATION_S)
    }

    static var dismiss: Animation {
        .easeOut(duration: AnimationConstants.shared.DISMISS_ANIM_DURATION_S)
    }
}
