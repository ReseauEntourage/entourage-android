package social.entourage.android.events.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.ui.create.CfGrey
import social.entourage.android.ui.create.CfGreyContour
import social.entourage.android.ui.create.CfOrange
import social.entourage.android.ui.create.CfText
import social.entourage.android.ui.create.CfWhite
import social.entourage.android.ui.create.cfOutlined
import social.entourage.android.ui.create.cfStyle
import social.entourage.android.ui.create.rememberNoIndication
import social.entourage.android.ui.theme.NunitoSansBold

/** Bascule présentiel / en ligne : deux moitiés, la sélection est pleine orange (rayon 10dp). */
@Composable
internal fun CreateEventModeToggle(
    online: Boolean,
    onSelect: (online: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .cfOutlined(RoundedCornerShape(14.dp), 1.dp, CfGreyContour, CfWhite)
            .padding(4.dp)
    ) {
        ToggleSegment(
            label = stringResource(R.string.face_to_face),
            selected = !online,
            onClick = { onSelect(false) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 2.dp)
                .testTag("create_event_face_to_face")
        )
        ToggleSegment(
            label = stringResource(R.string.online),
            selected = online,
            onClick = { onSelect(true) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp)
                .testTag("create_event_online")
        )
    }
}

@Composable
private fun ToggleSegment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .let { if (selected) it.background(CfOrange, RoundedCornerShape(10.dp)) else it }
            .selectable(
                selected = selected,
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CfText(
            label,
            cfStyle(NunitoSansBold, 15f, if (selected) CfWhite else CfGrey),
            textAlign = TextAlign.Center
        )
    }
}
