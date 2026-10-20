package social.entourage.android.e2e

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.R as MaterialR
import org.junit.After
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.onboarding.onboard.OnboardingPhase1Fragment

/**
 * Scénario E2E "sans connexion préalable" : sélection de la date de naissance à l'étape 1 de
 * l'inscription (EN-9438 : remplacement du DatePickerDialog natif par MaterialDatePicker).
 *
 * Le funnel d'inscription complet (jusqu'à cet écran) passe par une vérification SMS non
 * automatisable et n'est couvert par aucun scénario e2e existant : on lance donc
 * OnboardingPhase1Fragment isolément via FragmentScenario plutôt que de reproduire tout le
 * funnel, ce qui suffit à couvrir l'usage du nouveau composant de date.
 *
 * Le picker s'ouvre directement en mode calendrier (retour recette EN-9438).
 *
 * ATTENTION : les ids `com.google.android.material.R.id.mtrl_calendar_frame` /
 * `mtrl_picker_header_toggle` / `mtrl_picker_edit_text_date` / `confirm_button` sont ceux de
 * MaterialDatePicker (vérifiés dans le R.txt de material 1.14.0) — mais ce test a été écrit
 * sans émulateur/appareil disponible pour vérifier le déroulé complet : à lancer et ajuster sur un vrai appareil avant de merger
 * (même précaution que GroupCommentScenarioTest).
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class OnboardingBirthdateScenarioTest {

    private lateinit var scenario: FragmentScenario<OnboardingPhase1Fragment>

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun selectionnerUneDateDeNaissanceViaMaterialDatePicker() {
        scenario = launchFragmentInContainer(themeResId = R.style.AppMaterial)

        onView(withId(R.id.ui_onboard_birthdate)).perform(click())

        // EN-9438 (retour recette) : le calendrier doit s'afficher directement au clic sur le
        // champ, sans passer par l'icône de bascule.
        onView(withId(MaterialR.id.mtrl_calendar_frame)).check(matches(isDisplayed()))

        // Bascule en saisie manuelle (plus fiable en test que de taper une cellule du calendrier)
        onView(withId(MaterialR.id.mtrl_picker_header_toggle)).perform(click())
        onView(withId(MaterialR.id.mtrl_picker_edit_text_date)).perform(replaceText(pickerInputFor(15, 6, 1990)))
        onView(withId(MaterialR.id.confirm_button)).perform(click())

        onView(withId(R.id.ui_onboard_birthdate)).check(matches(withText("15/06/1990")))
    }

    /**
     * Le champ texte de MaterialDatePicker attend le format de date court de la locale de
     * l'appareil (MM/dd/yyyy en en-US, dd/MM/yyyy en fr-FR…), normalisé comme le fait
     * UtcDates.getDefaultTextInputFormat() (jour/mois sur 2 chiffres, année sur 4). Le champ
     * de l'app, lui, affiche toujours dd/MM/yyyy.
     */
    private fun pickerInputFor(day: Int, month: Int, year: Int): String {
        val pattern = (DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) as SimpleDateFormat)
            .toPattern()
            .replace(Regex("\\s+"), "")
            .replace(Regex("d+"), "dd")
            .replace(Regex("M+"), "MM")
            .replace(Regex("y+"), "yyyy")
        val calendar = Calendar.getInstance().apply { set(year, month - 1, day) }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
    }
}
