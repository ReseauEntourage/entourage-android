package social.entourage.android.configuration;

/**
 * Configuration class that will be subclassed in each app
 * Created by Mihai Ionescu on 02/05/2018.
 */
public abstract class BaseConfiguration {

    boolean showTutorial = false;
    boolean showMyMessagesFAB = false;

    protected BaseConfiguration() {}

    public boolean showMyMessagesFAB() {
        return showMyMessagesFAB;
    }

    public boolean showTutorial() {
        return showTutorial;
    }

}
