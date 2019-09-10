package social.entourage.android.entourage.create;

import social.entourage.android.entourage.category.EntourageCategory;

/**
 * Created by mihaiionescu on 18/05/2017.
 */

public interface CreateEntourageListener {

    void onTitleChanged(String title);
    void onDescriptionChanged(String description);
    void onCategoryChosen(EntourageCategory category);

}
