package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun InfoView(
    infoViewData: InfoViewData,
    primaryActionText: String,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = infoViewData.infoViewType.containerColor(),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = mokoImage(infoViewData.icon),
                contentDescription = null,
                tint = infoViewData.infoViewType.contentColor(),
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            text = mokoString(infoViewData.title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = Dimens.ExtraLarge,
                    start = Dimens.Large,
                    end = Dimens.Large,
                ),
        )
        Text(
            text = mokoString(infoViewData.message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = Dimens.Large,
                    start = Dimens.Huge,
                    end = Dimens.Huge,
                ),
        )
        Button(
            onClick = onPrimaryActionClick,
            modifier = Modifier
                .padding(top = Dimens.ExtraLarge),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimens.Medium),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                text = primaryActionText,
            )
        }
    }
}

@Composable
private fun InfoViewType.contentColor() =
    when (this) {
        InfoViewType.ERROR -> mokoColor(SharedRes.colors.error)
        InfoViewType.WARNING -> mokoColor(SharedRes.colors.warning)
        InfoViewType.SUCCESS -> mokoColor(SharedRes.colors.success)
        InfoViewType.INFO -> mokoColor(SharedRes.colors.info)
    }

@Composable
private fun InfoViewType.containerColor() =
    when (this) {
        InfoViewType.ERROR -> mokoColor(SharedRes.colors.errorContainer)
        InfoViewType.WARNING -> mokoColor(SharedRes.colors.warningContainer)
        InfoViewType.SUCCESS -> mokoColor(SharedRes.colors.successContainer)
        InfoViewType.INFO -> mokoColor(SharedRes.colors.infoContainer)
    }

@Composable
private fun InfoViewPreviewContent(
    infoViewType: InfoViewType,
    title: StringResource,
    message: StringResource,
    icon: ImageResource,
) {
    HuKiTheme {
        InfoView(
            infoViewData = InfoViewData(
                infoViewType = infoViewType,
                icon = icon,
                title = title,
                message = message,
            ),
            primaryActionText = mokoString(SharedRes.strings.search_error_retry),
            onPrimaryActionClick = {},
        )
    }
}

@Preview(name = "Error")
@Composable
private fun ErrorInfoViewPreview() {
    InfoViewPreviewContent(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_no_internet_title,
        message = SharedRes.strings.network_error_no_internet_message,
        icon = SharedRes.images.ic_no_internet,
    )
}

@Preview(name = "Warning")
@Composable
private fun WarningInfoViewPreview() {
    InfoViewPreviewContent(
        infoViewType = InfoViewType.WARNING,
        title = SharedRes.strings.network_error_title,
        message = SharedRes.strings.network_error_request_timeout_message,
        icon = SharedRes.images.ic_error,
    )
}

@Preview(name = "Success")
@Composable
private fun SuccessInfoViewPreview() {
    InfoViewPreviewContent(
        infoViewType = InfoViewType.SUCCESS,
        title = SharedRes.strings.network_error_title,
        message = SharedRes.strings.network_error_unknown_message,
        icon = SharedRes.images.ic_error,
    )
}

@Preview(name = "Info")
@Composable
private fun InfoInfoViewPreview() {
    InfoViewPreviewContent(
        infoViewType = InfoViewType.INFO,
        title = SharedRes.strings.network_error_not_found_title,
        message = SharedRes.strings.network_error_not_found_message,
        icon = SharedRes.images.ic_not_found,
    )
}
