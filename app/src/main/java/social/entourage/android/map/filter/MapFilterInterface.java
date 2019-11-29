package social.entourage.android.map.filter;

/**
 * Created by mihaiionescu on 17/05/16.
 */
public interface MapFilterInterface {

    String getTypes();
    int getTimeFrame();
    boolean showPastEvents();

    void entourageCreated();
    void validateCategories();

}
