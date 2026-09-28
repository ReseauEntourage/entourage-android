package social.entourage.android.afterLogin

import android.content.Context
import android.os.SystemClock
import androidx.core.content.edit
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageTestWithAPI
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.home.HomeFragment.Companion.PREF_ENHANCED_ONBOARDING_COMPLETED
import social.entourage.android.home.HomeFragment.Companion.PREF_GATING_ENHANCED_ONBOARDING_SHOWN
import timber.log.Timber

open class EntourageTestAfterLogin : EntourageTestWithAPI() {
    private val login: String = BuildConfig.TEST_ACCOUNT_LOGIN
    private val password: String = BuildConfig.TEST_ACCOUNT_PWD

    private var isIntentsInit = false


    protected fun checkUserIsLoggedIn(phoneNumber: String? = null, codePwd: String? = null) {
        if (!EntourageApplication.get().authenticationController.isAuthenticated) {
            login(phoneNumber ?: login, codePwd ?: password)
        }
    }

    private fun login(phoneNumber: String? = null, codePwd: String? = null) {
        val phoneNumber = phoneNumber ?: BuildConfig.TEST_ACCOUNT_LOGIN
        val codePwd = codePwd ?: BuildConfig.TEST_ACCOUNT_PWD
        runBlocking {
            withContext(Dispatchers.IO) {
                OnboardingAPI.getInstance().syncLogin(phoneNumber, codePwd) { isOK, _, error ->
                    if (!isOK) {
                        Timber.e("Login failed for test: $error")
                        throw Exception("Login should not fail: $error")
                    }
                }
            }
        }
    }

    open fun closeAutofill() {
    }

    @Before
    fun initIntents() {
        if (!isIntentsInit) {
            Intents.init()
            isIntentsInit = true
        }
    }

    override fun setUp(activity: Context) {
        super.setUp(activity)
        initIntents()
        checkUserIsLoggedIn()
    }


    @After
    override fun tearDown() {
        if (isIntentsInit) {
            Intents.release()
            isIntentsInit = false
        }
        super.tearDown()
    }

    protected fun forceOnboarding(done: Boolean = true) {
        EntourageApplication.get().sharedPreferences.edit(commit = true) {
            putBoolean(PREF_GATING_ENHANCED_ONBOARDING_SHOWN, done)
            putBoolean(PREF_ENHANCED_ONBOARDING_COMPLETED, done)
        }
    }

    protected fun checkNoPopUpOnHome() {
        SystemClock.sleep(200) // Brief check for potential popups
        checkNoOnboarding()
        checkNoActionPopUp(R.string.custom_dialog_action_title_one_contrib)
        checkNoActionPopUp(R.string.custom_dialog_action_title_one_demand)
    }

    protected fun checkNoActionPopUp(id: Int) {
        try {
            onView(allOf(withText(id),isDisplayed()))
            onView(allOf(withText(R.string.no),isDisplayed()))
                .perform(click())
            onView(allOf(withId(R.id.btn_cross),isDisplayed()))
                .perform(click())

        } catch (e: Exception) {
            //No onboarding
            Timber.d(e)
        }
    }

    protected fun checkNoOnboarding() {
        try {
            onView(allOf(withText(R.string.onboarding_presentation_btn_negative),isDisplayed()))
                .perform(click())
        } catch (e: Exception) {
            //No onboarding
            Timber.d(e)
        }
    }

    protected fun fetchGroupIdFromApi(): Int {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.groupRequest
                .getAllGroups(
                    page = 1,
                    per = 10,
                )
                .execute()

            response.body()?.allGroups?.firstOrNull()?.id
        } ?: throw IllegalStateException("Aucun groupe trouvé depuis l'API")
    }

    protected fun getNeighborhoodId(): String {
        cachedNeighborhoodId?.let { return it }
        checkUserIsLoggedIn()

        val id = runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.groupRequest
                .getAllGroups(
                    page = 1,
                    per = 10,
                )
                .execute()

            response.body()?.allGroups?.firstOrNull()?.let { group ->
                group.uuid_v2 ?: group.id?.toString()
            }
        } ?: throw IllegalStateException("Aucun groupe trouvé depuis l'API")
        cachedNeighborhoodId = id
        return id
    }

    protected fun getMyResourceId(): String {
        cachedResourceId?.let { return it }
        checkUserIsLoggedIn()

        val id = runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.homeRequest
                .getPedagogicalResources(false)
                .execute()

            response.body()?.pedago?.firstOrNull()?.let { pedago ->
                pedago.uuid_v2 ?: pedago.id?.toString()
            }
        } ?: throw IllegalStateException("Aucune ressource trouvée depuis l'API")
        cachedResourceId = id
        return id
    }

    protected fun getOutingId(): String {
        cachedOutingId?.let { return it }
        checkUserIsLoggedIn()

        val id = runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.eventsRequest
                .getAllEvents(
                    page = 1,
                    per = 10,
                    travelDistance = null,
                    latitude = null,
                    longitude = null,
                    period = "all",
                )
                .execute()

            response.body()?.allEvents?.firstOrNull()?.let { event ->
                event.uuid_v2 ?: event.id?.toString()
            }
        } ?: throw IllegalStateException("Aucun événement trouvé depuis l'API")
        cachedOutingId = id
        return id
    }

    private companion object {
        private var cachedNeighborhoodId: String? = null
        private var cachedResourceId: String? = null
        private var cachedOutingId: String? = null
    }
}