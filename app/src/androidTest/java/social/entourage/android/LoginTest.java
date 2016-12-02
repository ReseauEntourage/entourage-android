package social.entourage.android;

import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jakewharton.espresso.OkHttp3IdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.OkHttpClient;
import social.entourage.android.authentication.login.LoginActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {
    @Rule public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);
    private IdlingResource resource;

    @Before
    public void setUp() {
        Context context = activityRule.getActivity();
        OkHttpClient client = EntourageApplication.get(context).getEntourageComponent().getOkHttpClient();
        resource = OkHttp3IdlingResource.create("OkHttp", client);
        Espresso.registerIdlingResources(resource);
    }

    @After
    public void tearDown() throws Exception {
        Espresso.unregisterIdlingResources(resource);
    }

    @Test
    public void login() {
        onView(withId(R.id.login_button_login)).perform(click());
        onView(withId(R.id.login_edit_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN));
        onView(withId(R.id.login_edit_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD));

        onView(withId(R.id.login_button_signup)).perform(click());

        onView(withText(R.string.login_error)).check(doesNotExist());
    }
}
