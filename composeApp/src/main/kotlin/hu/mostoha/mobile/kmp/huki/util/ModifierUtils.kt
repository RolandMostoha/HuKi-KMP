package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Sets a Maestro-resolvable id on this node. Required inside Popups/Dialogs (DropdownMenu,
 * AlertDialog), whose separate composition window does not inherit the root testTagsAsResourceId.
 */
fun Modifier.testTagAsResourceId(tag: String): Modifier =
    semantics {
        testTagsAsResourceId = true
        testTag = tag
    }
