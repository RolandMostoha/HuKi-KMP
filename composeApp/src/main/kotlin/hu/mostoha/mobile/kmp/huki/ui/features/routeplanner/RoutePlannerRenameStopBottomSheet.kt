package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.secondarySheetColor
import hu.mostoha.mobile.kmp.huki.ui.components.RoundedTextField
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private const val COMMENT_MIN_LINES = 2
private const val COMMENT_MAX_LINES = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerRenameStopBottomSheet(
    stopNumber: Int,
    resolvedName: String,
    currentName: String,
    currentComment: String,
    address: String?,
    onDone: (name: String, comment: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = secondarySheetColor,
        dragHandle = null,
        modifier = modifier.testTagAsResourceId(TestTags.ROUTE_PLANNER_RENAME_SHEET),
    ) {
        RoutePlannerRenameStopContent(
            stopNumber = stopNumber,
            resolvedName = resolvedName,
            currentName = currentName,
            currentComment = currentComment,
            address = address,
            onDone = onDone,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun RoutePlannerRenameStopContent(
    stopNumber: Int,
    resolvedName: String,
    currentName: String,
    currentComment: String,
    address: String?,
    onDone: (name: String, comment: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentName, TextRange(currentName.length)))
    }
    var comment by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentComment, TextRange(currentComment.length)))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = Dimens.ExtraLarge),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.Large, top = Dimens.Large, end = Dimens.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        ) {
            FilledIconButton(
                onClick = onCancel,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = mokoString(SharedRes.strings.route_planner_rename_cancel),
                )
            }
            Text(
                text = mokoString(SharedRes.strings.route_planner_rename_title, stopNumber),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onDone(name.text, comment.text) },
                modifier = Modifier.testTagAsResourceId(TestTags.ROUTE_PLANNER_RENAME_DONE),
            ) {
                Text(text = mokoString(SharedRes.strings.route_planner_rename_save))
            }
        }
        Column(
            modifier = Modifier.padding(start = Dimens.Large, top = Dimens.SmallMedium, end = Dimens.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            RoundedTextField(
                value = name,
                onValueChange = { name = it },
                label = mokoString(SharedRes.strings.route_planner_rename_name),
                placeholder = resolvedName,
                supportingText = address?.let { mokoString(SharedRes.strings.route_planner_rename_address, it) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone(name.text, comment.text) }),
                fieldModifier = Modifier
                    .testTagAsResourceId(TestTags.ROUTE_PLANNER_RENAME_NAME),
            )
            RoundedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = mokoString(SharedRes.strings.route_planner_rename_comment),
                placeholder = mokoString(SharedRes.strings.route_planner_rename_comment_placeholder),
                singleLine = false,
                minLines = COMMENT_MIN_LINES,
                maxLines = COMMENT_MAX_LINES,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                fieldModifier = Modifier.testTagAsResourceId(TestTags.ROUTE_PLANNER_RENAME_COMMENT),
            )
        }
    }
}

@Preview(locale = "hu")
@Composable
private fun RoutePlannerRenameStopContentPreview() {
    HuKiTheme {
        RoutePlannerRenameStopContent(
            stopNumber = 2,
            resolvedName = "Háromkő-hegy",
            currentName = "Háromkő-hegy",
            currentComment = "",
            address = "Háromkő-hegy, Pilisszentkereszt",
            onDone = { _, _ -> },
            onCancel = {},
        )
    }
}
