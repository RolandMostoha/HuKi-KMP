package hu.mostoha.mobile.kmp.huki.ui.features.gpxcollection

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
internal fun DeleteConfirmDialog(fileName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = mokoString(SharedRes.strings.gpx_collection_delete_dialog_title)) },
        text = { Text(text = mokoString(SharedRes.strings.gpx_collection_delete_dialog_message, fileName)) },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTagAsResourceId(TestTags.GPX_COLLECTION_DELETE_DIALOG_CONFIRM_BUTTON),
                onClick = onConfirm,
            ) {
                Text(
                    text = mokoString(SharedRes.strings.gpx_collection_action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = mokoString(SharedRes.strings.gpx_collection_delete_dialog_cancel))
            }
        },
    )
}
