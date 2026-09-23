package social.entourage.android.tools.utils

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import social.entourage.android.R
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val BIRTHDATE_PICKER_TAG = "birthdate_picker"
private const val DEFAULT_OPEN_YEARS_AGO = 30
private val BIRTHDATE_REGEX = Regex("""\d{2}/\d{2}/\d{4}""")

/**
 * MaterialDatePicker ouvert directement en mode calendrier (INPUT_MODE_CALENDAR) : en recette,
 * le mode saisie texte n'était pas intuitif (il fallait trouver l'icône calendrier). La saisie
 * au clavier reste accessible via l'icône de bascule du dialogue, et l'année se choisit en
 * touchant l'en-tête mois/année.
 * Sans date existante, le calendrier s'ouvre [DEFAULT_OPEN_YEARS_AGO] ans en arrière, sans
 * présélectionner de date, pour limiter la navigation mois par mois.
 * Travaille en Calendar UTC pour ne pas décaler le jour affiché selon le fuseau local.
 * Affiche/retourne des dates au format dd/MM/yyyy.
 * Passe explicitement ThemeOverlay.App.MaterialCalendar au picker : le thème par défaut de
 * l'app (AppTheme) hérite de Theme.AppCompat et ne fournit pas l'attribut materialCalendarTheme
 * requis par MaterialDatePicker (crash sinon dans les écrans qui n'utilisent pas AppMaterial,
 * comme EditProfileActivity).
 */
fun showBirthdateDatePicker(
    fragmentManager: FragmentManager,
    currentDateText: String?,
    titleText: CharSequence,
    onDateSelected: (String) -> Unit
) {
    if (fragmentManager.findFragmentByTag(BIRTHDATE_PICKER_TAG) != null) return

    val utcTimeZone = TimeZone.getTimeZone("UTC")

    val currentSelection: Long? = currentDateText
        ?.takeIf { it.matches(BIRTHDATE_REGEX) }
        ?.split("/")?.let { (dd, mm, yyyy) ->
            runCatching {
                Calendar.getInstance(utcTimeZone).apply {
                    set(yyyy.toInt(), mm.toInt() - 1, dd.toInt(), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.getOrNull()
        }

    val openAt = currentSelection ?: Calendar.getInstance(utcTimeZone).apply {
        add(Calendar.YEAR, -DEFAULT_OPEN_YEARS_AGO)
    }.timeInMillis

    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .setOpenAt(openAt)
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(titleText)
        .apply { currentSelection?.let { setSelection(it) } }
        .setCalendarConstraints(constraints)
        .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
        .setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        .build()

    picker.addOnPositiveButtonClickListener { selectionUtcMillis ->
        val selected = Calendar.getInstance(utcTimeZone).apply { timeInMillis = selectionUtcMillis }
        onDateSelected(
            String.format(
                Locale.getDefault(), "%02d/%02d/%04d",
                selected.get(Calendar.DAY_OF_MONTH),
                selected.get(Calendar.MONTH) + 1,
                selected.get(Calendar.YEAR)
            )
        )
    }

    picker.show(fragmentManager, BIRTHDATE_PICKER_TAG)
}
