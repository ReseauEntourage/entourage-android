package social.entourage.android.configuration;

/**
 * Created by Mihai Ionescu on 02/05/2018.
 */
public class Configuration extends BaseConfiguration {

    private static Configuration _instance = null;

    public static Configuration getInstance() {
        if (_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }

    protected Configuration() {
        showTutorial = true;
        showMyMessagesFAB = true;
    }

}
