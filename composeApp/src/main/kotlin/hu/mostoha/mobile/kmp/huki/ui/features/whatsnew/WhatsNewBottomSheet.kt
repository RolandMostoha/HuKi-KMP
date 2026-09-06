package hu.mostoha.mobile.kmp.huki.ui.features.whatsnew

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNewMessage
import hu.mostoha.mobile.kmp.huki.model.mapper.toReleaseNoteLines
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.VersionPill
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.LocalizedDateFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewBottomSheet(
    whatsNew: WhatsNew,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = null,
    ) {
        Column(
            modifier = modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.Large)
                .padding(top = Dimens.Large, bottom = Dimens.ExtraLarge)
                .testTagAsResourceId(TestTags.WHATS_NEW_SHEET),
            verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        ) {
            WhatsNewHeader(
                version = whatsNew.version,
                releaseDate = whatsNew.releaseDate,
                onCloseClick = onDismissRequest,
            )
            WhatsNewNotesCard(releaseNotes = whatsNew.releaseNotes)
            whatsNew.message?.let { message ->
                WhatsNewMessageCard(message = message)
            }
        }
    }
}

@Composable
private fun WhatsNewHeader(version: String, releaseDate: LocalDate, onCloseClick: () -> Unit) {
    val monthYear = remember(releaseDate) { LocalizedDateFormatter().formatMonthYear(releaseDate) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(SharedRes.images.ic_app_icon.drawableResId),
            contentDescription = mokoString(SharedRes.strings.whats_new_a11y_app_icon),
            modifier = Modifier
                .size(Dimens.IconHero * 0.6f)
                .shadow(
                    elevation = Dimens.ExtraSmall,
                    shape = RoundedCornerShape(Dimens.MediumLarge),
                    clip = true,
                ),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = Dimens.MediumLarge)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Small),
        ) {
            Text(
                text = mokoString(SharedRes.strings.whats_new_title),
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                VersionPill(text = "v$version")
                Text(
                    text = monthYear,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Dimens.Medium),
                )
            }
        }
        IconButton(
            onClick = onCloseClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.testTagAsResourceId(TestTags.WHATS_NEW_CLOSE_BUTTON),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = mokoString(SharedRes.strings.a11y_close),
            )
        }
    }
}

@Composable
private fun WhatsNewNotesCard(releaseNotes: StringDesc) {
    Column(
        modifier = Modifier
            .padding(
                vertical = Dimens.Small,
                horizontal = Dimens.Large,
            )
            .testTagAsResourceId(TestTags.WHATS_NEW_RELEASE_NOTES),
        verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        mokoString(releaseNotes).toReleaseNoteLines().forEach { note ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Dimens.Small)
                        .background(color = mokoColor(SharedRes.colors.primary), shape = CircleShape),
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimens.MediumLarge),
                )
            }
        }
    }
}

@Composable
private fun WhatsNewMessageCard(message: WhatsNewMessage) {
    val accentColor = mokoColor(SharedRes.colors.primary)
    Surface(
        shape = RoundedCornerShape(Dimens.MediumLarge),
        color = accentColor,
        modifier = Modifier
            .fillMaxWidth()
            .testTagAsResourceId(TestTags.WHATS_NEW_MESSAGE_CARD),
    ) {
        Surface(
            shape = RoundedCornerShape(Dimens.MediumLarge),
            color = mokoColor(SharedRes.colors.primaryContainer),
            modifier = Modifier.padding(start = Dimens.ExtraSmall),
        ) {
            Column(
                modifier = Modifier.padding(
                    vertical = Dimens.MediumLarge,
                    horizontal = Dimens.Large,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Small),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_heart_double),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = mokoString(message.title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = accentColor,
                        modifier = Modifier.padding(start = Dimens.SmallMedium),
                    )
                }
                Text(
                    text = mokoString(message.body),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = mokoString(SharedRes.strings.whats_new_message_signature),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = accentColor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WhatsNewBottomSheetPreview() {
    HuKiTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        ) {
            WhatsNewHeader(
                version = previewWhatsNew.version,
                releaseDate = previewWhatsNew.releaseDate,
                onCloseClick = {},
            )
            WhatsNewNotesCard(releaseNotes = previewWhatsNew.releaseNotes)
            previewWhatsNew.message?.let { WhatsNewMessageCard(message = it) }
        }
    }
}

private val previewWhatsNew = WhatsNew(
    version = "0.9",
    releaseDate = LocalDate(2026, 7, 16),
    releaseNotes = StringDesc.Resource(SharedRes.strings.menu_app_description),
    message = WhatsNewMessage(
        title = StringDesc.Resource(SharedRes.strings.menu_app_name),
        body = StringDesc.Resource(SharedRes.strings.menu_app_description),
    ),
)
