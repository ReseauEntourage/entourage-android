package social.entourage.android.actions.create

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils
import social.entourage.android.ui.create.CfBeige
import social.entourage.android.ui.create.CfBlack
import social.entourage.android.ui.create.CfError
import social.entourage.android.ui.create.CfGlideImage
import social.entourage.android.ui.create.CfGrey
import social.entourage.android.ui.create.CfHint
import social.entourage.android.ui.create.CfLabelRow
import social.entourage.android.ui.create.CfOptionCard
import social.entourage.android.ui.create.CfOrange
import social.entourage.android.ui.create.CfPhotoEmpty
import social.entourage.android.ui.create.CfPreviewChip
import social.entourage.android.ui.create.CfRadioRow
import social.entourage.android.ui.create.CfSelectField
import social.entourage.android.ui.create.CfStepColumn
import social.entourage.android.ui.create.CfText
import social.entourage.android.ui.create.CfTextField
import social.entourage.android.ui.create.CfTitle
import social.entourage.android.ui.create.cfStyle
import social.entourage.android.ui.create.nunitoLineHeight
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

/** Nom de l'entraide dans les textes : « demande » ou « contribution ». */
@Composable
private fun actionName(isDemand: Boolean): String =
    stringResource(if (isDemand) R.string.action_name_demand else R.string.action_name_contrib)

// --- Étape : catégorie ------------------------------------------------------------------------

@Composable
internal fun CreateActionStepCategory(
    state: CreateActionUiState,
    errors: Map<CreateActionField, Int>,
    onOpenAbout: () -> Unit,
) {
    val context = LocalContext.current
    val metaData by MetaDataRepository.metaData.observeAsState()
    val isDemand = state.viewModel.isDemand
    val form = state.form
    val name = actionName(isDemand)

    CfStepColumn {
        CfTitle(stringResource(R.string.action_create_cat_choose_category, name))
        CfLabelRow(stringResource(R.string.create_action_category_label), Modifier.padding(bottom = 12.dp))

        metaData?.sections?.forEach { section ->
            val id = section.id ?: return@forEach
            CfOptionCard(
                icon = ActionSection.getIconFromId(id),
                title = EventUtils.showTagTranslated(context, id),
                description = EventUtils.showSubTagTranslated(context, id),
                selected = form.sectionId == id,
                onToggle = {
                    state.form.sectionId = id
                    state.changed()
                },
                modifier = Modifier.testTag("create_action_category_$id")
            )
        }
        CfError(errors[CreateActionField.CATEGORY], tag = "create_action_category_error")

        // Lien vers la page pédagogique : « Votre contribution ne rentre pas dans ces catégories ? ».
        val about = buildAnnotatedString {
            append(stringResource(R.string.action_crete_cat_about, name))
            withLink(LinkAnnotation.Clickable(tag = "about", linkInteractionListener = { onOpenAbout() })) {
                withStyle(SpanStyle(color = CfOrange, textDecoration = TextDecoration.Underline)) {
                    append(stringResource(R.string.action_create_cat_about_link).trim())
                }
            }
        }
        BasicText(
            text = about,
            style = cfStyle(NunitoSansRegular, 13f, CfGrey, lineHeight = nunitoLineHeight(13f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .testTag("create_action_about")
        )
    }
}

// --- Étape : titre, description, photo --------------------------------------------------------

@Composable
internal fun CreateActionStepDescription(
    state: CreateActionUiState,
    errors: Map<CreateActionField, Int>,
    onChoosePhoto: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = state.viewModel
    val isDemand = viewModel.isDemand
    val form = state.form
    val name = actionName(isDemand)
    var title by remember { mutableStateOf(form.title) }
    var description by remember { mutableStateOf(form.description) }

    // Exemples de saisie propres à la catégorie choisie, avec un repli générique.
    val (titleHint, descriptionHint) = placeholdersFor(isDemand, form.sectionId, context).let { (t, d) ->
        (t.ifEmpty { stringResource(R.string.action_create_title_hint, name) }) to
            (d.ifEmpty { stringResource(R.string.action_create_description_hint, name) })
    }

    CfStepColumn {
        CfTitle(R.string.create_action_step_description_title)

        CfLabelRow(stringResource(R.string.create_action_title_label), Modifier.padding(bottom = 4.dp))
        CfHint(stringResource(R.string.action_create_subtitle, name), Modifier.padding(bottom = 9.dp))
        CfTextField(
            value = title,
            onValueChange = {
                title = it
                state.form.title = it
                state.changed()
            },
            hint = titleHint,
            hasError = errors[CreateActionField.TITLE] != null,
            maxLines = 3,
            modifier = Modifier.testTag("create_action_name"),
        )
        CfError(errors[CreateActionField.TITLE], tag = "create_action_name_error")

        CfLabelRow(
            stringResource(R.string.create_event_description_label),
            Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        CfTextField(
            value = description,
            onValueChange = {
                val value = it.take(CreateActionForm.MAX_DESCRIPTION_LENGTH)
                description = value
                state.form.description = value
                state.changed()
            },
            hint = descriptionHint,
            hasError = errors[CreateActionField.DESCRIPTION] != null,
            minHeight = 112.dp,
            topAligned = true,
            maxLength = CreateActionForm.MAX_DESCRIPTION_LENGTH,
            modifier = Modifier.testTag("create_action_description"),
        )
        CfText(
            stringResource(R.string.create_action_description_counter, description.length.toString()),
            cfStyle(NunitoSansRegular, 11f, CfGrey),
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            textAlign = TextAlign.End
        )
        CfError(errors[CreateActionField.DESCRIPTION], tag = "create_action_description_error")

        // La photo n'existe que pour une contribution.
        if (!isDemand) {
            CfLabelRow(
                stringResource(R.string.create_action_photo_label),
                Modifier.padding(top = 20.dp, bottom = 8.dp),
                required = false,
                optional = true
            )
            val source: Any? = form.imageUri ?: form.imageUrl?.let { Uri.parse(it) }
            if (source != null) {
                CfGlideImage(
                    source = source,
                    contentDescription = stringResource(R.string.picture),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable(role = Role.Button, onClick = onChoosePhoto)
                        .testTag("create_action_photo")
                )
            } else {
                CfPhotoEmpty(
                    title = stringResource(R.string.create_event_photo_add),
                    hint = null,
                    hasError = false,
                    onClick = onChoosePhoto,
                    modifier = Modifier.testTag("create_action_add_photo")
                )
            }
        }
    }
}

/** Titre et description d'exemple, selon la catégorie et le type d'entraide. */
private fun placeholdersFor(isDemand: Boolean, sectionId: String?, context: android.content.Context): Pair<String, String> {
    fun pair(title: Int, description: Int) = context.getString(title) to context.getString(description)
    return if (isDemand) {
        when (sectionId) {
            ActionSection.social -> pair(R.string.demand_social_title, R.string.demand_social_description)
            ActionSection.clothes -> pair(R.string.demand_clothes_title, R.string.demand_clothes_description)
            ActionSection.equipment -> pair(R.string.demand_equipment_title, R.string.demand_equipment_description)
            ActionSection.hygiene -> pair(R.string.demand_hygiene_title, R.string.demand_hygiene_description)
            ActionSection.services -> pair(R.string.demand_services_title, R.string.demand_services_description)
            else -> "" to ""
        }
    } else {
        when (sectionId) {
            ActionSection.social -> pair(R.string.contrib_social_title, R.string.contrib_social_description)
            ActionSection.clothes -> pair(R.string.contrib_clothes_title, R.string.contrib_clothes_description)
            ActionSection.equipment -> pair(R.string.contrib_equipment_title, R.string.contrib_equipment_description)
            ActionSection.hygiene -> pair(R.string.contrib_hygiene_title, R.string.contrib_hygiene_description)
            ActionSection.services -> pair(R.string.contrib_services_title, R.string.contrib_services_description)
            else -> "" to ""
        }
    }
}

// --- Étape : lieu -----------------------------------------------------------------------------

@Composable
internal fun CreateActionStepLocation(
    state: CreateActionUiState,
    errors: Map<CreateActionField, Int>,
    onPickAddress: () -> Unit,
) {
    val form = state.form
    val name = actionName(state.viewModel.isDemand)

    CfStepColumn {
        CfTitle(stringResource(R.string.action_create_location_title, name))

        CfLabelRow(stringResource(R.string.create_event_address_label), Modifier.padding(bottom = 4.dp))
        CfHint(stringResource(R.string.action_create_location_subtitle), Modifier.padding(bottom = 9.dp))
        CfSelectField(
            text = form.address,
            hint = stringResource(R.string.add_location_hint),
            trailingIcon = R.drawable.ic_location,
            hasError = errors[CreateActionField.LOCATION] != null,
            onClick = onPickAddress,
            iconPadding = 10.dp,
            modifier = Modifier.testTag("create_action_location")
        )
        CfError(errors[CreateActionField.LOCATION], tag = "create_action_location_error")
    }
}

// --- Étape : partage dans le groupe par défaut ------------------------------------------------

@Composable
internal fun CreateActionStepSharing(
    state: CreateActionUiState,
    errors: Map<CreateActionField, Int>,
) {
    val form = state.form
    val name = actionName(state.viewModel.isDemand)
    val groupName = state.viewModel.defaultGroupName ?: ""

    CfStepColumn {
        CfTitle(stringResource(R.string.share_request_group, name, groupName))
        CfHint(stringResource(R.string.share_request_description, name), Modifier.padding(bottom = 12.dp))

        CfRadioRow(
            label = stringResource(R.string.yes),
            selected = form.shareInGroup == true,
            onClick = {
                state.form.shareInGroup = true
                state.changed()
            },
            modifier = Modifier.testTag("create_action_share_yes")
        )
        CfRadioRow(
            label = stringResource(R.string.no),
            selected = form.shareInGroup == false,
            onClick = {
                state.form.shareInGroup = false
                state.changed()
            },
            modifier = Modifier.testTag("create_action_share_no")
        )
        CfError(errors[CreateActionField.SHARING], topMargin = 12.dp, tag = "create_action_share_error")
    }
}

// --- Aperçu -----------------------------------------------------------------------------------

@Composable
internal fun CreateActionPreview(state: CreateActionUiState) {
    val context = LocalContext.current
    val form = state.form
    val isDemand = state.viewModel.isDemand
    val source: Any? = form.imageUri ?: form.imageUrl?.let { Uri.parse(it) }

    CfStepColumn {
        CfTitle(stringResource(R.string.create_action_preview_title, actionName(isDemand)), sizeSp = 21f)

        if (!isDemand && source != null) {
            CfGlideImage(
                source = source,
                contentDescription = stringResource(R.string.picture),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .background(CfBeige, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(14.dp))
        }

        form.sectionId?.let { CfPreviewChip(EventUtils.showTagTranslated(context, it)) }

        CfText(
            form.title,
            cfStyle(QuicksandBold, 18f, CfBlack),
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp)
                .testTag("create_action_preview_title")
        )

        if (!form.address.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(R.drawable.ic_location), contentDescription = null)
                Spacer(Modifier.width(9.dp))
                CfText(form.address ?: "", cfStyle(NunitoSansRegular, 14f, CfGrey))
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
