package social.entourage.android.map.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 * Created by mihaiionescu on 17/05/16.
 */
public interface MapFilterInterface {

    String getTypes();
    boolean onlyMyEntourages();
    int getTimeFrame();
    boolean onlyMyPartnerEntourages();
    boolean showPastEvents();

    void entourageCreated();
    void validateCategories();

}
