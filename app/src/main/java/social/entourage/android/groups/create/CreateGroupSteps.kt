package social.entourage.android.groups.create

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EventUtils
import social.entourage.android.ui.create.CfBeige
import social.entourage.android.ui.create.CfError
import social.entourage.android.ui.create.CfGlideImage
import social.entourage.android.ui.create.CfGrey
import social.entourage.android.ui.create.CfHint
import social.entourage.android.ui.create.CfInterestChip
import social.entourage.android.ui.create.CfLabelRow
import social.entourage.android.ui.create.CfPhotoEmpty
import social.entourage.android.ui.create.CfPreviewChip
import social.entourage.android.ui.create.CfSelectField
import social.entourage.android.ui.create.CfStepColumn
import social.entourage.android.ui.create.CfText
import social.entourage.android.ui.create.CfTextField
import social.entourage.android.ui.create.CfTitle
import social.entourage.android.ui.create.cfStyle
import social.entourage.android.ui.create.nunitoLineHeight
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold
import social.entourage.android.ui.create.CfBlack

// --- Étape 1 : nom, description, lieu ---------------------------------------------------------

@Composable
internal fun CreateGroupStepInfo(
    state: CreateGroupUiState,
    errors: Map<CreateGroupField, Int>,
    onPickAddress: () -> Unit,
) {
    val form = state.form
    var name by remember { mutableStateOf(form.name) }
    var description by remember { mutableStateOf(form.description) }

    CfStepColumn {
        CfTitle(R.string.create_group_step_one_title)

        CfLabelRow(stringResource(R.string.group_name), Modifier.padding(bottom = 8.dp))
        CfTextField(
            value = name,
            onValueChange = {
                name = it
                state.form.name = it
                state.changed()
            },
            hint = stringResource(R.string.group_name_hint),
            hasError = errors[CreateGroupField.NAME] != null,
            maxLines = 3,
            modifier = Modifier.testTag("create_group_name"),
        )
        CfError(errors[CreateGroupField.NAME], tag = "create_group_name_error")

        CfLabelRow(
            stringResource(R.string.create_event_description_label),
            Modifier.padding(top = 20.dp, bottom = 4.dp)
        )
        CfHint(stringResource(R.string.create_group_description_hint), Modifier.padding(bottom = 9.dp))
        CfTextField(
            value = description,
            onValueChange = {
                val value = it.take(CreateGroupForm.MAX_DESCRIPTION_LENGTH)
                description = value
                state.form.description = value
                state.changed()
            },
            hint = stringResource(R.string.create_group_description_placeholder),
            hasError = errors[CreateGroupField.DESCRIPTION] != null,
            minHeight = 112.dp,
            topAligned = true,
            maxLength = CreateGroupForm.MAX_DESCRIPTION_LENGTH,
            modifier = Modifier.testTag("create_group_description"),
        )
        CfText(
            stringResource(R.string.description_counter, description.length.toString()),
            cfStyle(NunitoSansRegular, 11f, CfGrey),
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            textAlign = TextAlign.End
        )
        CfError(errors[CreateGroupField.DESCRIPTION], tag = "create_group_description_error")

        // Le lieu n'est pas modifiable en édition (il n'est pas envoyé à la mise à jour).
        if (!state.viewModel.isEdition) {
            CfLabelRow(
                stringResource(R.string.add_location),
                Modifier.padding(top = 20.dp, bottom = 4.dp)
            )
            CfHint(stringResource(R.string.add_location_subtitle), Modifier.padding(bottom = 9.dp))
            CfSelectField(
                text = form.address,
                hint = stringResource(R.string.add_location_hint),
                trailingIcon = R.drawable.ic_location,
                hasError = errors[CreateGroupField.LOCATION] != null,
                onClick = onPickAddress,
                iconPadding = 10.dp,
                modifier = Modifier.testTag("create_group_location")
            )
            CfError(errors[CreateGroupField.LOCATION], tag = "create_group_location_error")
        }
    }
}

// --- Étape 2 : intérêts -----------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateGroupStepInterests(
    state: CreateGroupUiState,
    errors: Map<CreateGroupField, Int>,
) {
    val context = LocalContext.current
    val tags by MetaDataRepository.metaData.observeAsState()
    val form = state.form
    var otherInterest by remember { mutableStateOf(form.otherInterest) }

    CfStepColumn {
        CfTitle(R.string.create_group_step_two_title)
        CfHint(stringResource(R.string.choose_categories_group), Modifier.padding(bottom = 14.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            tags?.interests?.forEach { tag ->
                val id = tag.id ?: return@forEach
                CfInterestChip(
                    label = EventUtils.showTagTranslated(context, id),
                    selected = form.interests.contains(id),
                    onToggle = {
                        val interests = state.form.interests
                        if (interests.contains(id)) interests.remove(id) else interests.add(id)
                        state.changed()
                    },
                    modifier = Modifier.testTag("create_group_interest_$id")
                )
            }
        }
        CfError(errors[CreateGroupField.INTERESTS], topMargin = 12.dp, tag = "create_group_interests_error")

        // Précision de la catégorie « Autre » : proposée en création uniquement, comme avant.
        if (!state.viewModel.isEdition && form.interests.contains(CreateGroupForm.OTHER_INTEREST)) {
            CfLabelRow(
                stringResource(R.string.category_name_label),
                Modifier.padding(top = 24.dp, bottom = 8.dp),
                required = false,
                optional = true
            )
            CfTextField(
                value = otherInterest,
                onValueChange = {
                    otherInterest = it
                    state.form.otherInterest = it
                    state.changed()
                },
                hint = "",
                hasError = false,
                maxLines = 3,
                modifier = Modifier.testTag("create_group_other_interest"),
            )
        }
    }
}

// --- Étape 3 : message de bienvenue et photo --------------------------------------------------

@Composable
internal fun CreateGroupStepPhoto(
    state: CreateGroupUiState,
    errors: Map<CreateGroupField, Int>,
    onChoosePhoto: () -> Unit,
) {
    val form = state.form
    var welcome by remember { mutableStateOf(form.welcomeMessage) }
    val isEdition = state.viewModel.isEdition

    CfStepColumn {
        CfTitle(R.string.create_group_step_three_title)

        CfLabelRow(
            stringResource(R.string.group_welcome_message),
            Modifier.padding(bottom = 8.dp),
            required = false,
            optional = true
        )
        CfTextField(
            value = welcome,
            onValueChange = {
                welcome = it
                state.form.welcomeMessage = it
                state.changed()
            },
            hint = stringResource(R.string.group_welcome_message),
            hasError = false,
            minHeight = 112.dp,
            topAligned = true,
            maxLines = 4,
            modifier = Modifier.testTag("create_group_welcome"),
        )

        CfLabelRow(
            stringResource(if (isEdition) R.string.edit_photo else R.string.group_choose_photo),
            Modifier.padding(top = 20.dp, bottom = 4.dp),
            required = !isEdition
        )
        if (!isEdition) {
            CfHint(stringResource(R.string.group_photo_label), Modifier.padding(bottom = 9.dp))
        }
        val photoUrl = form.photoUrl
        if (photoUrl != null) {
            CfGlideImage(
                source = Uri.parse(photoUrl),
                contentDescription = stringResource(R.string.picture),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(role = Role.Button, onClick = onChoosePhoto)
                    .testTag("create_group_photo")
            )
        } else {
            CfPhotoEmpty(
                title = stringResource(R.string.create_event_photo_add),
                hint = null,
                hasError = errors[CreateGroupField.PHOTO] != null,
                onClick = onChoosePhoto,
                modifier = Modifier.testTag("create_group_add_photo")
            )
        }
        CfError(errors[CreateGroupField.PHOTO], tag = "create_group_photo_error")
    }
}

// --- Aperçu -----------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateGroupPreview(state: CreateGroupUiState) {
    val context = LocalContext.current
    val form = state.form

    val source: Any = form.photoUrl?.let { Uri.parse(it) } ?: R.drawable.ic_event_placeholder

    CfStepColumn {
        CfTitle(R.string.create_group_preview_title, sizeSp = 21f)

        CfGlideImage(
            source = source,
            contentDescription = stringResource(R.string.picture),
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .background(CfBeige, RoundedCornerShape(12.dp))
        )

        CfText(
            form.name,
            cfStyle(QuicksandBold, 18f, CfBlack),
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp)
                .testTag("create_group_preview_title")
        )

        if (!form.address.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null
                )
                Spacer(Modifier.width(9.dp))
                CfText(form.address ?: "", cfStyle(NunitoSansRegular, 14f, CfGrey))
            }
        }

        if (form.interests.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                form.interests.forEach { CfPreviewChip(EventUtils.showTagTranslated(context, it)) }
            }
        }

        CfText(
            form.description,
            cfStyle(NunitoSansRegular, 15f, CfBlack, lineHeight = nunitoLineHeight(15f)),
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        )
    }
}
