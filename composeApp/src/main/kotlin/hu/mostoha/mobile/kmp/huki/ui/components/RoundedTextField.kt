package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun RoundedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val shape = if (singleLine) HuKiTextFieldDefaults.singleLineShape else HuKiTextFieldDefaults.multiLineShape
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.Large),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(text = it) } },
            trailingIcon = {
                if (value.text.isNotEmpty()) {
                    IconButton(onClick = { onValueChange(TextFieldValue()) }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_clear),
                            contentDescription = mokoString(SharedRes.strings.a11y_clear_text),
                        )
                    }
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            shape = shape,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = HuKiTextFieldDefaults.colors(),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = Dimens.Large),
            )
        }
    }
}

@Preview
@Composable
private fun RoundedTextFieldPreview() {
    HuKiTheme {
        Column(
            modifier = Modifier.padding(Dimens.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            RoundedTextField(
                value = TextFieldValue("Háromkő-hegy", TextRange("Háromkő-hegy".length)),
                onValueChange = {},
                label = "Name",
                supportingText = "Original address: Háromkő-hegy, Pilisszentkereszt",
            )
            RoundedTextField(
                value = TextFieldValue(),
                onValueChange = {},
                label = "Comment",
                placeholder = "E.g. there's a spring here",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
            )
        }
    }
}
