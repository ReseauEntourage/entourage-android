package social.entourage.android.map.entourage;

/**
 * Created by mihaiionescu on 18/05/2017.
 */

public interface CreateEntourageListener {

    public void onTitleChanged(String title);
    public void onDescriptionChanged(String description);
    public void onCategoryChosen(String entourageType, String category);

}
