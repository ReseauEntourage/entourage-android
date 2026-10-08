package social.entourage.android.events.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.flow.drop
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Interest
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Utils
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_DURATION_HOURS = 3L
private const val MAX_DESCRIPTION_LENGTH = 900

// --- Étape 1 : nom, description, photo --------------------------------------------------------

@Composable
internal fun CreateEventStepPresentation(
    state: CreateEventUiState,
    errors: Map<CreateEventField, Int>,
    onChoosePhoto: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = state.viewModel

    // L'échec d'envoi n'est annoncé que lorsque cette étape est affichée, comme avant.
    val uploadFailed by viewModel.uploadFailed.observeAsState(false)
    LaunchedEffect(uploadFailed) {
        if (uploadFailed == true) {
            Utils.showToast(context, context.getString(R.string.create_event_photo_upload_error))
            viewModel.consumeUploadFailed()
            state.refresh()
        }
    }

    var title by remember { mutableStateOf(state.form.title) }
    var description by remember { mutableStateOf(state.form.description) }

    CeStepColumn {
        CeTitle(R.string.create_event_step_one_title)

        // Nom
        CeLabelRow(stringResource(R.string.event_name), Modifier.padding(bottom = 8.dp))
        CeTextField(
            value = title,
            onValueChange = {
                title = it
                state.form.title = it
                state.changed()
            },
            hint = stringResource(R.string.create_event_name_placeholder),
            hasError = errors[CreateEventField.NAME] != null,
            maxLines = 3,
            modifier = Modifier.testTag("create_event_name"),
        )
        CeError(errors[CreateEventField.NAME], tag = "create_event_name_error")

        // Description
        CeLabelRow(
            stringResource(R.string.create_event_description_label),
            Modifier.padding(top = 20.dp, bottom = 4.dp)
        )
        CeHint(
            stringResource(R.string.create_event_description_hint),
            Modifier.padding(bottom = 9.dp)
        )
        CeTextField(
            value = description,
            onValueChange = {
                val value = it.take(MAX_DESCRIPTION_LENGTH)
                description = value
                state.form.description = value
                state.changed()
            },
            hint = stringResource(R.string.create_event_description_placeholder),
            hasError = errors[CreateEventField.DESCRIPTION] != null,
            minHeight = 112.dp,
            topAligned = true,
            modifier = Modifier.testTag("create_event_description"),
        )
        CeText(
            stringResource(R.string.events_description_counter, description.length.toString()),
            ceStyle(NunitoSansRegular, 11f, CeGrey),
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            textAlign = TextAlign.End
        )
        CeError(errors[CreateEventField.DESCRIPTION], tag = "create_event_description_error")

        // Photo
        CeLabelRow(
            stringResource(R.string.picture),
            Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        val form = state.form
        val source: Any? = form.localPhotoPath?.let { File(it) } ?: form.photoUrl?.let { Uri.parse(it) }
        if (source != null) {
            CeGlideImage(
                source = source,
                contentDescription = stringResource(R.string.picture),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(role = Role.Button, onClick = onChoosePhoto)
                    .testTag("create_event_photo")
            )
        } else {
            // Photo déjà envoyée mais sans aperçu disponible (brouillon restauré) : on le dit.
            val alreadyChosen = form.hasPhoto()
            CePhotoEmpty(
                title = stringResource(
                    if (alreadyChosen) R.string.create_event_photo_added else R.string.create_event_photo_add
                ),
                hint = stringResource(R.string.create_event_photo_hint),
                hasError = errors[CreateEventField.PHOTO] != null,
                onClick = onChoosePhoto,
                modifier = Modifier.testTag("create_event_add_photo")
            )
        }
        CeError(errors[CreateEventField.PHOTO], tag = "create_event_photo_error")
    }
}

// --- Étape 2 : date, heures, récurrence -------------------------------------------------------

@Composable
internal fun CreateEventStepWhen(
    state: CreateEventUiState,
    errors: Map<CreateEventField, Int>,
) {
    val context = LocalContext.current
    val locale = LanguageManager.getLocaleFromPreferences(context)
    val dateFormat = stringResource(R.string.events_date)
    val timeFormat = stringResource(R.string.events_time)
    val form = state.form

    fun formatDate(date: LocalDate): String = DateTimeFormatter.ofPattern(dateFormat, locale).format(date)
    fun formatTime(time: LocalTime): String = DateTimeFormatter.ofPattern(timeFormat, locale).format(time)

    fun pickDate() {
        val current = CreateEventForm.parseDate(state.form.date) ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                state.form.date = LocalDate.of(year, month + 1, day).toString()
                state.changed()
            },
            current.year, current.monthValue - 1, current.dayOfMonth
        ).run {
            datePicker.minDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            show()
        }
    }

    // Sans valeur saisie, le sélecteur propose une durée par défaut de DEFAULT_DURATION_HOURS heures
    // autour de l'autre heure, comme avant la refonte.
    fun pickTime(isStart: Boolean) {
        val f = state.form
        val own = CreateEventForm.parseTime(if (isStart) f.startTime else f.endTime)
        val other = CreateEventForm.parseTime(if (isStart) f.endTime else f.startTime)
        val initial = own
            ?: other?.plusHours(if (isStart) -DEFAULT_DURATION_HOURS else DEFAULT_DURATION_HOURS)
            ?: LocalTime.now()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val value = LocalTime.of(hour, minute).toString()
                if (isStart) f.startTime = value else f.endTime = value
                state.changed()
            },
            initial.hour, initial.minute, true
        ).show()
    }

    // La récurrence reste réservée aux utilisateurs autorisés (ceux qui ont un rôle).
    val me = EntourageApplication.me(context)
    val recurrenceAllowed = me?.roles?.isEmpty() != true

    val start = errors[CreateEventField.START_TIME]
    val end = errors[CreateEventField.END_TIME]

    CeStepColumn {
        CeTitle(R.string.create_event_step_two_title)

        // Date
        CeLabelRow(stringResource(R.string.date), Modifier.padding(bottom = 8.dp))
        CeSelectField(
            text = CreateEventForm.parseDate(form.date)?.let { formatDate(it) },
            hint = stringResource(R.string.date_hint),
            trailingIcon = R.drawable.ic_event_date,
            hasError = errors[CreateEventField.DATE] != null,
            onClick = ::pickDate,
            modifier = Modifier.testTag("create_event_date")
        )
        CeError(errors[CreateEventField.DATE], tag = "create_event_date_error")

        // Heures
        Row(Modifier.padding(top = 20.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
            ) {
                CeLabelRow(stringResource(R.string.start_time), Modifier.padding(bottom = 8.dp), required = false)
                CeSelectField(
                    text = CreateEventForm.parseTime(form.startTime)?.let { formatTime(it) },
                    hint = stringResource(R.string.time_hint),
                    trailingIcon = R.drawable.ic_event_time,
                    hasError = start != null,
                    onClick = { pickTime(isStart = true) },
                    modifier = Modifier.testTag("create_event_start_time")
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            ) {
                CeLabelRow(stringResource(R.string.end_time), Modifier.padding(bottom = 8.dp), required = false)
                CeSelectField(
                    text = CreateEventForm.parseTime(form.endTime)?.let { formatTime(it) },
                    hint = stringResource(R.string.time_hint),
                    trailingIcon = R.drawable.ic_event_time,
                    hasError = end != null,
                    onClick = { pickTime(isStart = false) },
                    modifier = Modifier.testTag("create_event_end_time")
                )
            }
        }
        CeError(firstErrorOf(errors, CreateEventField.START_TIME, CreateEventField.END_TIME), tag = "create_event_time_error")

        // Récurrence
        if (recurrenceAllowed) {
            CeText(
                stringResource(R.string.recurrence),
                CeLabelStyle,
                Modifier.padding(top = 24.dp)
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                listOf(
                    Triple(Recurrence.NO_RECURRENCE.value, R.string.juste_once, "create_event_recurrence_once"),
                    Triple(Recurrence.EVERY_WEEK.value, R.string.every_week, "create_event_recurrence_week"),
                    Triple(Recurrence.EVERY_TWO_WEEKS.value, R.string.every_two_week, "create_event_recurrence_two_weeks"),
                ).forEach { (value, label, tag) ->
                    val selected = when (form.recurrence) {
                        Recurrence.EVERY_WEEK.value -> value == Recurrence.EVERY_WEEK.value
                        Recurrence.EVERY_TWO_WEEKS.value -> value == Recurrence.EVERY_TWO_WEEKS.value
                        else -> value == Recurrence.NO_RECURRENCE.value
                    }
                    CeRadioRow(
                        label = stringResource(label),
                        selected = selected,
                        onClick = {
                            state.form.recurrence = value
                            state.changed()
                        },
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        }
    }
}

// --- Étape 3 : où et pour qui -----------------------------------------------------------------

@Composable
internal fun CreateEventStepWhere(
    state: CreateEventUiState,
    errors: Map<CreateEventField, Int>,
    onPickAddress: () -> Unit,
) {
    val form = state.form
    val online = form.online
    var eventUrl by remember { mutableStateOf(state.form.eventUrl) }
    val placeError = errors[CreateEventField.PLACE]

    CeStepColumn {
        CeTitle(R.string.create_event_step_three_title)

        // Bascule présentiel / en ligne : passer en ligne annule la carte fauteuil (sans objet).
        CeModeToggle(
            online = online,
            onSelect = { isOnline ->
                state.form.setOnlineMode(isOnline)
                state.changed()
            }
        )

        // Adresse ou lien
        CeLabelRow(
            stringResource(if (online) R.string.create_event_link_label else R.string.create_event_address_label),
            Modifier.padding(bottom = 8.dp)
        )
        if (online) {
            CeTextField(
                value = eventUrl,
                onValueChange = {
                    eventUrl = it
                    state.form.eventUrl = it
                    state.changed()
                },
                hint = stringResource(R.string.create_event_link_hint),
                hasError = placeError != null,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                modifier = Modifier.testTag("create_event_url"),
            )
        } else {
            CeSelectField(
                text = form.address,
                hint = stringResource(R.string.create_event_address_hint),
                trailingIcon = R.drawable.ic_location,
                hasError = placeError != null,
                onClick = onPickAddress,
                iconPadding = 10.dp,
                modifier = Modifier.testTag("create_event_location")
            )
        }
        CeError(placeError, tag = "create_event_place_error")

        // Nombre de places
        CeText(
            stringResource(R.string.create_event_places_label),
            CeLabelStyle,
            Modifier.padding(top = 20.dp)
        )
        val limit = form.placeLimit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CeText(
                stringResource(R.string.create_event_places_row),
                ceStyle(NunitoSansRegular, 15f, CeBlack),
                Modifier.weight(1f)
            )
            CeRoundButton(
                size = 40.dp,
                icon = R.drawable.ic_create_event_minus,
                description = stringResource(R.string.create_event_places_minus),
                modifier = Modifier
                    .ceDim(limit != null)
                    .testTag("create_event_places_minus"),
                onClick = {
                    val f = state.form
                    val current = f.placeLimit
                    if (current != null) {
                        // En dessous de 1, on revient à « Non » (pas de limite).
                        f.placeLimit = if (current > 1) current - 1 else null
                        state.changed()
                    }
                }
            )
            CeText(
                text = limit?.toString() ?: stringResource(R.string.create_event_places_none),
                style = ceStyle(social.entourage.android.ui.theme.NunitoSansBold, 16f, CeBlack),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .defaultMinSize(minWidth = 48.dp)
                    .testTag("create_event_places_value"),
                textAlign = TextAlign.Center
            )
            CeRoundButton(
                size = 40.dp,
                icon = R.drawable.ic_create_event_plus,
                description = stringResource(R.string.create_event_places_plus),
                modifier = Modifier.testTag("create_event_places_plus"),
                onClick = {
                    val f = state.form
                    f.placeLimit = (f.placeLimit ?: 0) + 1
                    state.changed()
                }
            )
        }
        CeError(errors[CreateEventField.PLACE_LIMIT], tag = "create_event_places_error")

        // Accessibilité et public
        CeText(
            stringResource(if (online) R.string.create_event_public else R.string.create_event_access_and_public),
            CeLabelStyle,
            Modifier.padding(top = 24.dp, bottom = 12.dp)
        )
        // La carte fauteuil n'a pas de sens pour un événement en ligne.
        if (!online) {
            CeOptionCard(
                icon = R.drawable.ic_create_event_wheelchair,
                title = stringResource(R.string.create_event_card_wheelchair),
                description = stringResource(R.string.create_event_card_wheelchair_desc),
                selected = form.wheelchairAccessible,
                onToggle = {
                    state.form.wheelchairAccessible = !state.form.wheelchairAccessible
                    state.changed()
                },
                modifier = Modifier.testTag("create_event_card_wheelchair")
            )
        }
        CeOptionCard(
            icon = R.drawable.ic_create_event_family,
            title = stringResource(R.string.create_event_card_family),
            description = stringResource(R.string.create_event_card_family_desc),
            selected = form.familyFriendly,
            onToggle = {
                state.form.familyFriendly = !state.form.familyFriendly
                state.changed()
            },
            modifier = Modifier.testTag("create_event_card_family")
        )
        CeOptionCard(
            icon = R.drawable.ic_create_event_female,
            title = stringResource(R.string.create_event_card_female),
            description = null,
            selected = form.reservedFemale,
            onToggle = {
                state.form.reservedFemale = !state.form.reservedFemale
                state.changed()
            },
            modifier = Modifier.testTag("create_event_card_female")
        )
    }
}

// --- Étape 4 : catégories ---------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateEventStepCategories(
    state: CreateEventUiState,
    errors: Map<CreateEventField, Int>,
) {
    val context = LocalContext.current
    val tags by MetaDataRepository.metaData.observeAsState()
    val form = state.form

    CeStepColumn {
        CeTitle(R.string.create_event_step_four_title)
        CeHint(
            stringResource(R.string.choose_categories_event),
            Modifier.padding(bottom = 14.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            tags?.interests?.forEach { tag ->
                val id = tag.id ?: return@forEach
                CeInterestChip(
                    label = EventUtils.showTagTranslated(context, id),
                    icon = Interest.getIconFromId(id),
                    selected = form.interests.contains(id),
                    onToggle = {
                        val interests = state.form.interests
                        if (interests.contains(id)) interests.remove(id) else interests.add(id)
                        state.changed()
                    },
                    modifier = Modifier.testTag("create_event_interest_$id")
                )
            }
        }
        CeError(errors[CreateEventField.CATEGORIES], topMargin = 12.dp, tag = "create_event_categories_error")
    }
}

// --- Étape 5 : partage dans les groupes -------------------------------------------------------

@Composable
internal fun CreateEventStepSharing(
    state: CreateEventUiState,
    groups: List<Group>,
    onShown: () -> Unit,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(Unit) { onShown() }
    val listState = rememberLazyListState()
    // Pagination au défilement, comme l'ancien OnScrollListener : on charge la page suivante
    // quand le bas de la liste est atteint.
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                info.visibleItemsInfo.size + listState.firstVisibleItemIndex >= info.totalItemsCount
            )
        }
            .drop(1)
            .collect { (_, _, atEnd) -> if (atEnd) onLoadMore() }
    }
    val form = state.form

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 6.dp)
    ) {
        CeText(
            stringResource(R.string.create_event_step_five_title),
            ceStyle(QuicksandBold, 24f, CeBlack),
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, bottom = 10.dp)
        )
        CeText(
            stringResource(R.string.create_event_step_five_hint),
            CeHintStyle,
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, bottom = 8.dp)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("create_event_groups"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 22.dp, end = 22.dp, bottom = 16.dp)
        ) {
            items(groups, key = { it.id ?: it.hashCode() }) { group ->
                val selected = group.id?.let { form.neighborhoodIds.contains(it) } == true
                GroupRow(
                    name = group.name ?: "",
                    selected = selected,
                    onToggle = {
                        group.id?.let { id ->
                            val ids = state.form.neighborhoodIds
                            if (ids.contains(id)) ids.remove(id) else ids.add(id)
                            state.changed()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupRow(name: String, selected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .padding(horizontal = 2.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CeText(
            name,
            ceStyle(NunitoSansRegular, 14f, CeBlack, lineHeight = nunitoLineHeight(14f)),
            Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        CeCheckSquare(selected)
    }
}

// --- Aperçu -----------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateEventPreview(state: CreateEventUiState) {
    val context = LocalContext.current
    val locale = LanguageManager.getLocaleFromPreferences(context)
    val timeFormat = stringResource(R.string.events_time)
    val form = state.form

    val source: Any = form.localPhotoPath?.let { File(it) }
        ?: form.photoUrl?.let { Uri.parse(it) }
        ?: R.drawable.ic_event_placeholder

    val dateLine = run {
        val date = CreateEventForm.parseDate(form.date)
        if (date == null) "" else {
            val start = CreateEventForm.parseTime(form.startTime)
            val end = CreateEventForm.parseTime(form.endTime)
            val dateText = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(date)
            if (start == null || end == null) dateText else {
                val format = DateTimeFormatter.ofPattern(timeFormat, locale)
                stringResource(
                    R.string.create_event_preview_time_range,
                    dateText, format.format(start), format.format(end)
                )
            }
        }
    }

    // Pastilles d'accessibilité / de public renseignées, puis catégories. Les pastilles fauteuil et
    // famille s'affichent ici même si elles ne sont pas encore envoyées à l'API.
    val chips = buildList {
        if (form.wheelchairAccessible && !form.online) add(stringResource(R.string.create_event_preview_chip_wheelchair))
        if (form.familyFriendly) add(stringResource(R.string.create_event_preview_chip_family))
        if (form.reservedFemale) add(stringResource(R.string.create_event_preview_chip_female))
        form.interests.forEach { add(EventUtils.showTagTranslated(context, it)) }
    }

    CeStepColumn {
        CeTitle(R.string.create_event_preview_title, sizeSp = 21f)

        CeGlideImage(
            source = source,
            contentDescription = stringResource(R.string.picture),
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .background(CeBeige, RoundedCornerShape(12.dp))
        )

        CeText(
            form.title,
            ceStyle(QuicksandBold, 18f, CeBlack),
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp)
                .testTag("create_event_preview_title")
        )

        PreviewLine(R.drawable.ic_event_date, dateLine, Modifier.padding(bottom = 6.dp))
        PreviewLine(R.drawable.ic_location, if (form.online) form.eventUrl else form.address ?: "")

        if (chips.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                chips.forEach { CePreviewChip(it) }
            }
        }

        CeText(
            form.description,
            ceStyle(NunitoSansRegular, 15f, CeBlack, lineHeight = nunitoLineHeight(15f)),
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        )

        val count = form.neighborhoodIds.size
        if (count > 0) {
            CeText(
                pluralStringResource(R.plurals.create_event_preview_groups_nudge, count, count),
                ceStyle(NunitoSansRegular, 13f, CeGrey),
                Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .ceOutlined(RoundedCornerShape(20.dp), 1.dp, CeGreyContour, CeWhite)
                    .padding(13.dp)
            )
        }
    }
}

@Composable
private fun PreviewLine(icon: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(icon), contentDescription = null)
        Spacer(Modifier.width(9.dp))
        CeText(text, ceStyle(NunitoSansRegular, 14f, CeGrey))
    }
}
