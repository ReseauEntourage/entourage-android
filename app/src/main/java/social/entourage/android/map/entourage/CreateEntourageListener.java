package social.entourage.android.map.entourage;

import social.entourage.android.map.entourage.category.EntourageCategory;

/**
 * Created by mihaiionescu on 18/05/2017.
 */

public interface CreateEntourageListener {

    public void onTitleChanged(String title);
    public void onDescriptionChanged(String description);
    public void onCategoryChosen(EntourageCategory category);

}
