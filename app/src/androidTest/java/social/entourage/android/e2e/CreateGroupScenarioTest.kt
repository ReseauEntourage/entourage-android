package social.entourage.android.e2e

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.groups.create.CommunicationHandlerViewModel
import social.entourage.android.groups.create.CreateGroupActivity
import timber.log.Timber

/**
 * Scénario E2E "connecté" : crée réellement un groupe de voisins de bout en bout contre le backend
 * de staging (étape 1 nom/description/lieu -> étape 2 catégorie -> étape 3 photo -> création ->
 * écran de succès).
 *
 * Nettoyage : l'API n'expose pas de suppression de groupe ; le test quitte le groupe créé
 * (`leaveGroup`) pour ne pas encombrer la liste du compte de test. Le groupe lui-même reste côté
 * staging (nom horodaté "Unit test group ...").
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateGroupScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("creation_groupe")

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private var createdGroupName: String? = null

    @After
    fun cleanupCreatedGroup() {
        val name = createdGroupName ?: return
        try {
            val userId = EntourageApplication.get().me()?.id ?: return
            val groupRequest = EntourageApplication.get().apiModule.groupRequest
            val id = groupRequest.getMyGroups(userId, 1, 50).execute().body()?.allGroups
                ?.firstOrNull { it.name == name }?.id ?: return
            groupRequest.leaveGroup(id).execute()
        } catch (e: Exception) {
            Timber.tag("E2E").e(e, "Echec du nettoyage du groupe cree par le test")
        }
    }

    @Test
    fun creationGroupe() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenario = ActivityScenario.launch<CreateGroupActivity>(
            Intent(context, CreateGroupActivity::class.java)
        )
        scenario.onActivity { activity -> setUp(activity) }

        // Les categories de l'etape 2 et les photos de l'etape 3 viennent de l'API (chargees au demarrage par
        // MainActivity, absente ici) : on les charge et on attend qu'elles soient disponibles.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            MetaDataRepository.getMetaData()
            MetaDataRepository.getGroupImages() // catalogue de photos de l'etape 3
        }
        val deadline = System.currentTimeMillis() + 20_000
        while ((MetaDataRepository.metaData.value == null || MetaDataRepository.groupImages.value.isNullOrEmpty()) &&
            System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(250)
        }
        assertNotNull("Metadonnees non chargees", MetaDataRepository.metaData.value)
        assertFalse("Catalogue de photos vide", MetaDataRepository.groupImages.value.isNullOrEmpty())
        screenshot.shoot("etape1_vide")

        val nom = "Unit test group ${System.currentTimeMillis()}"
        createdGroupName = nom
        onView(allOf(withId(R.id.group_name), isDisplayed())).perform(replaceText(nom), closeSoftKeyboard())
        onView(withId(R.id.group_description)).perform(
            replaceText("Groupe cree automatiquement par le test E2E."), closeSoftKeyboard()
        )
        screenshot.shoot("etape1_texte_rempli")

        // Le choix du lieu passe par le Geocoder Google (nom d'adresse -> coordonnees), qui ne repond
        // pas de facon fiable sur emulateur (l'ecran reste sur "Mon lieu" sans erreur). On renseigne
        // donc le lieu directement dans le ViewModel de l'assistant ; la creation, elle, est reelle.
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[CommunicationHandlerViewModel::class.java].group.apply {
                latitude = 48.8566
                longitude = 2.3522
                displayAddress = "Paris - 75004"
            }
        }
        screenshot.shoot("etape1_lieu_renseigne")

        onView(withId(R.id.next)).perform(click())
        onIdle()
        screenshot.shoot("etape2_categories")

        onView(allOf(withId(R.id.egs2_recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        screenshot.shoot("etape2_categorie_choisie")
        onView(withId(R.id.next)).perform(click())
        onIdle()
        screenshot.shoot("etape3_photo")

        onView(allOf(withId(R.id.egs3_add_photo_layout), isDisplayed())).perform(scrollTo(), click())
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        onView(withId(R.id.validate)).perform(click())
        onIdle()
        screenshot.shoot("etape3_photo_choisie")

        onView(withId(R.id.next)).check(matches(withText(R.string.create))).perform(click())
        onIdle()
        screenshot.shoot("creation_soumise")

        onView(withText(R.string.group_success_title)).check(matches(isDisplayed()))
        screenshot.shoot("succes")
        scenario.close()
    }
}
