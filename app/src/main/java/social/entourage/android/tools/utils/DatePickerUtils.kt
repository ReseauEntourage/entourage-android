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
private val BIRTHDATE_REGEX = Regex("""\d{2}/\d{2}/\d{4}""")

/**
 * MaterialDatePicker en mode saisie texte (INPUT_MODE_TEXT) : pour une date éloignée comme
 * une date de naissance, les guidelines M3 déconseillent le calendrier modal et recommandent
 * la saisie ("Don't use a modal date picker to prompt for dates in the distant past... use a
 * modal input picker" - https://m3.material.io/components/date-pickers/guidelines). Le
 * calendrier reste accessible via l'icône de bascule du dialogue.
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
    val utcCal = Calendar.getInstance(utcTimeZone)

    currentDateText
        ?.takeIf { it.matches(BIRTHDATE_REGEX) }
        ?.split("/")?.let { (dd, mm, yyyy) ->
            runCatching {
                utcCal.set(yyyy.toInt(), mm.toInt() - 1, dd.toInt(), 0, 0, 0)
                utcCal.set(Calendar.MILLISECOND, 0)
            }
        }

    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(titleText)
        .setSelection(utcCal.timeInMillis)
        .setCalendarConstraints(constraints)
        .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
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
