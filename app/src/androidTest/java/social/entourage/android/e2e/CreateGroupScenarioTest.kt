package social.entourage.android.e2e

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.groups.create.CreateGroupViewModel
import timber.log.Timber

/**
 * Scénario E2E "connecté" : crée réellement un groupe de voisins de bout en bout contre le backend
 * de staging (étape 1 nom/description/lieu -> étape 2 catégorie -> étape 3 photo -> aperçu ->
 * création -> écran de succès).
 *
 * L'assistant est en Compose : ses éléments se ciblent par `testTag` ("create_group_*", voir
 * CreateGroupScreen / CreateGroupSteps) via la règle Compose ; le catalogue de photos reste une View,
 * piloté par Espresso.
 *
 * Nettoyage : l'API n'expose pas de suppression de groupe ; le test quitte le groupe créé
 * (`leaveGroup`) pour ne pas encombrer la liste du compte de test. Le groupe lui-même reste côté
 * staging (nom horodaté "Unit test group ...").
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateGroupScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("creation_groupe")

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val activityRule = ActivityScenarioRule(CreateGroupActivity::class.java)

    private val composeTestRule = AndroidComposeTestRule(
        activityRule = activityRule,
        activityProvider = { rule ->
            var activity: CreateGroupActivity? = null
            rule.scenario.onActivity { activity = it }
            activity ?: error("Activity was not created")
        }
    )

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeTestRule)

    private var createdGroupName: String? = null

    private val isInterestChip = SemanticsMatcher("est une pastille de catégorie") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("create_group_interest_") == true
    }

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity -> super.setUp(activity) }
    }

    private fun tap(tag: String) {
        composeTestRule.onNodeWithTag(tag).performClick()
    }

    private fun type(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag).performTextInput(text)
        closeSoftKeyboard()
    }

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

        // Bouton jamais grise : sans rien saisir, "Continuer" affiche les erreurs sous les champs.
        tap("create_group_next")
        composeTestRule.onNodeWithTag("create_group_name_error").assertExists()
        screenshot.shoot("etape1_erreurs")

        val nom = "Unit test group ${System.currentTimeMillis()}"
        createdGroupName = nom
        type("create_group_name", nom)
        type("create_group_description", "Groupe cree automatiquement par le test E2E.")
        screenshot.shoot("etape1_texte_rempli")

        // Le choix du lieu passe par le Geocoder Google (nom d'adresse -> coordonnees), qui ne repond
        // pas de facon fiable sur emulateur. On renseigne donc le lieu directement dans le ViewModel de
        // l'assistant ; la creation, elle, est reelle.
        activityRule.scenario.onActivity { activity ->
            ViewModelProvider(activity)[CreateGroupViewModel::class.java].form.apply {
                latitude = 48.8566
                longitude = 2.3522
                address = "Paris - 75004"
            }
        }
        screenshot.shoot("etape1_lieu_renseigne")

        tap("create_group_next")
        onIdle()
        screenshot.shoot("etape2_categories")

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodes(isInterestChip).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(isInterestChip).onFirst().performClick()
        screenshot.shoot("etape2_categorie_choisie")
        tap("create_group_next")
        onIdle()
        screenshot.shoot("etape3_photo")

        tap("create_group_add_photo")
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        onView(withId(R.id.validate)).perform(click())
        onIdle()
        screenshot.shoot("etape3_photo_choisie")

        tap("create_group_next")
        onIdle()
        screenshot.shoot("apercu")

        // Dernier bouton : "Creer" (en creation), l'apercu precede l'envoi.
        composeTestRule.onNodeWithTag("create_group_next")
            .assertTextContains(composeTestRule.activity.getString(R.string.create))
        tap("create_group_next")
        onIdle()
        screenshot.shoot("creation_soumise")

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            try {
                onView(withText(R.string.group_success_title)).check(matches(isDisplayed()))
                true
            } catch (e: Throwable) {
                false
            }
        }
        screenshot.shoot("succes")
    }
}
