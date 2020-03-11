package social.entourage.android;

import android.content.Context;
import android.view.autofill.AutofillManager;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.jakewharton.espresso.OkHttp3IdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.OkHttpClient;
import social.entourage.android.authentication.login.LoginActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {
    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    private IdlingResource resource;
    private AutofillManager afM;

    @Before
    public void setUp() {
        checkNoUserIsLoggedIn();
        Context context = activityRule.getActivity();
        OkHttpClient client = EntourageApplication.get(context).getEntourageComponent().getOkHttpClient();
        resource = OkHttp3IdlingResource.create("OkHttp", client);
        IdlingRegistry.getInstance().register(resource);
        afM = context.getSystemService(AutofillManager.class);
        if(afM!=null) {
            afM.disableAutofillServices();
        }
    }

    private void checkTCDisplay() {
        //PFP we have a TC validation screen
        if(EntourageApplication.isPfpApp()) {
            onView(withId(R.id.register_welcome_start_button)).perform(click());
        }
    }

    private void checkNoUserIsLoggedIn() {
        Context context = activityRule.getActivity();
        if(EntourageApplication.get(context).getEntourageComponent().getAuthenticationController().getUser() != null) {
           EntourageApplication.get(context).getEntourageComponent().getAuthenticationController().logOutUser();
        }
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(resource);
        checkNoUserIsLoggedIn();
    }

    @Test
    public void loginOK() {
        checkNoUserIsLoggedIn();
        onView(withId(R.id.login_button_login)).perform(click());
        checkTCDisplay();

        onView(withId(R.id.login_edit_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_edit_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_button_signin)).perform(click());

        onView(withText(R.string.login_error_title)).check(doesNotExist());
        //checkNoUserIsLoggedIn();
    }

    @Test
    public void loginOKwithoutCountryCode() {
        onView(withId(R.id.login_button_login)).perform(click());
        checkTCDisplay();

        onView(withId(R.id.login_edit_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33", "0")), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_edit_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_button_signin)).perform(click());

        onView(withText(R.string.login_error_title)).check(doesNotExist());
        //checkNoUserIsLoggedIn();
    }

    private void closeAutofill() {
        if(afM ==null) {
            afM = activityRule.getActivity().getSystemService(AutofillManager.class);
        }
        if(afM!=null) {
            afM.cancel();
            afM.commit();
        }
    }

    @Test
    public void loginFailureWrongPassword() {
        checkNoUserIsLoggedIn();
        onView(withId(R.id.login_button_login)).perform(click());
        checkTCDisplay();

        onView(withId(R.id.login_edit_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_edit_code)).perform(typeText("999999"), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_button_signin)).perform(click());

        onView(withText(R.string.login_error_title)).check(matches(isDisplayed()));
        onView(withText(R.string.login_retry_label)).perform(click());
        onView(withId(R.id.login_back_button)).perform(click());
    }

    @Test
    public void loginFailureWrongPhoneNumberFormat() {
        onView(withId(R.id.login_button_login)).perform(click());
        checkTCDisplay();

        onView(withId(R.id.login_edit_phone)).perform(typeText("012345678"), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_edit_code)).perform(typeText("000000"), closeSoftKeyboard());
        closeAutofill();
        onView(withId(R.id.login_button_signin)).perform(click());

        onView(withText(R.string.login_error_title)).check(matches(isDisplayed()));
        onView(withText(R.string.login_retry_label)).perform(click());
        onView(withId(R.id.login_back_button)).perform(click());
    }
}
