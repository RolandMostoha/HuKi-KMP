package hu.mostoha.mobile.kmp.huki.ui.features.okt

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionItem
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun OktResumeFab(section: OktSectionItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = mokoString(SharedRes.strings.okt_a11y_resume, section.name)
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(Dimens.OktResumeFabSize)
            .semantics { contentDescription = description }
            .testTag(TestTags.OKT_RESUME_FAB),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Dimens.FloatingActionElevation),
        shape = CircleShape,
    ) {
        OktBadgeText(
            prefix = section.badgePrefix,
            number = section.badgeNumber,
            fontSize = 14.sp,
            color = mokoColor(SharedRes.colors.oktBlue),
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .clearAndSetSemantics {},
        )
    }
}

@Preview
@Composable
private fun OktResumeFabPreview() {
    HuKiTheme {
        OktResumeFab(section = PreviewOktSection, onClick = {}, modifier = Modifier.padding(Dimens.Large))
    }
}
