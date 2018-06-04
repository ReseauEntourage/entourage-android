package social.entourage.android.configuration;

/**
 * Configuration class that will be subclassed in each app
 * Created by Mihai Ionescu on 02/05/2018.
 */
public abstract class BaseConfiguration {

    // In case of an lost code error, show the toast or a separate screen
    boolean showLostCodeErrorToast = true;
    // Show the tutorial for the first time users
    boolean showTutorial = false;
    // Show + button in My Messages screen
    boolean showMyMessagesFAB = false;
    // Show the user edit screen
    boolean showUserEditProfile = true;

    protected BaseConfiguration() {}

    public boolean showLostCodeErrorToast() {
        return showLostCodeErrorToast;
    }

    public boolean showMyMessagesFAB() {
        return showMyMessagesFAB;
    }

    public boolean showTutorial() {
        return showTutorial;
    }

    public boolean showUserEditProfile() {
        return showUserEditProfile;
    }
}
