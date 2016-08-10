package social.entourage.android.map.entourage.my.filter;

/**
 * Created by mihaiionescu on 10/08/16.
 */
public class MyEntouragesFilter {

    public boolean activeEntourages = true;
    public boolean closedEntourages = true;
    public boolean showOwnEntourages = true;
    public boolean showJoinedEntourages= true;

    public boolean entourageTypeDemand = true;
    public boolean entourageTypeContribution = true;

    public boolean showTours = true;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MyEntouragesFilter ourInstance = new MyEntouragesFilter();

    public static MyEntouragesFilter getInstance() {
        return ourInstance;
    }

    private MyEntouragesFilter() {
    }
}
