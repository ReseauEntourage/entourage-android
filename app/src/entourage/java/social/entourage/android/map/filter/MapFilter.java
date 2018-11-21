package social.entourage.android.map.filter;

import java.io.Serializable;
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
public class MapFilter implements MapFilterInterface, Serializable {

    private static final long serialVersionUID = -2822136342813499636L;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public static final int DAYS_1 = 24; //hours
    public static final int DAYS_2 = 8*24; //hours
    public static final int DAYS_3 = 30*24; //hours

    public boolean tourTypeMedical = true;
    public boolean tourTypeSocial = true;
    public boolean tourTypeDistributive = true;

    public boolean entourageTypeOuting = true;
    public boolean showPastEvents = false;

    public boolean entourageTypeDemand = true;
    public boolean entourageTypeContribution = true;

    List<String> entourageCategories = new ArrayList<>();

    public boolean showTours = true;

    public boolean onlyMyEntourages = false;
    public boolean onlyMyPartnerEntourages = false;

    public int timeframe = DAYS_3;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MapFilter ourInstance = new MapFilter();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    protected MapFilter() {
        validateCategories();
    }

    // ----------------------------------
    // MapFilter implementation
    // ----------------------------------

    @Override
    public String getTypes() {
        StringBuilder entourageTypes = new StringBuilder();

        if (tourTypeMedical) {
            entourageTypes.append(TourType.MEDICAL.getKey());
        }
        if (tourTypeSocial) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append(TourType.BARE_HANDS.getKey());
        }
        if (tourTypeDistributive) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append(TourType.ALIMENTARY.getKey());
        }
        if (entourageTypeOuting) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append("ou");
        }
        for (String categoryKey: entourageCategories) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append(categoryKey);
        }

        return entourageTypes.toString();
    }

    @Override
    public boolean onlyMyEntourages() {
        return onlyMyEntourages;
    }

    @Override
    public int getTimeFrame() {
        return timeframe;
    }

    @Override
    public boolean onlyMyPartnerEntourages() {
        return onlyMyPartnerEntourages;
    }

    @Override
    public boolean showPastEvents() {
        return showPastEvents;
    }

    @Override
    public void entourageCreated() {
        entourageTypeContribution = true;
        entourageTypeDemand = true;
    }

    public void validateCategories() {
        if (entourageTypeDemand) validateCategoriesForType(Entourage.TYPE_DEMAND);
        if (entourageTypeContribution) validateCategoriesForType(Entourage.TYPE_CONTRIBUTION);
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    public boolean isCategoryChecked(EntourageCategory entourageCategory) {
        if (Entourage.TYPE_DEMAND.equals(entourageCategory.getEntourageType())) return entourageTypeDemand && entourageCategories.contains(entourageCategory.getKey());
        if (Entourage.TYPE_CONTRIBUTION.equals(entourageCategory.getEntourageType())) return entourageTypeContribution && entourageCategories.contains(entourageCategory.getKey());
        return false;
    }

    public void setCategoryChecked(String actionCategory, boolean checked) {
        if (checked) {
            if (!entourageCategories.contains(actionCategory)) {
                entourageCategories.add(actionCategory);
            }
        } else {
            entourageCategories.remove(actionCategory);
        }
    }

    // ----------------------------------
    // Serialization
    // ----------------------------------

    protected void validateCategoriesForType(String actionType) {
        //get the list of categories for this type
        EntourageCategoryManager categoryManager = EntourageCategoryManager.getInstance();
        List<EntourageCategory> categoryList = categoryManager.getEntourageCategoriesForType(actionType);
        if (categoryList != null) {
            Iterator<EntourageCategory> iterator = categoryList.iterator();
            boolean allKeysFalse = true;
            //search for the keys
            while (iterator.hasNext()) {
                EntourageCategory category = iterator.next();
                if (entourageCategories.contains(category.getKey())) {
                    allKeysFalse = false;
                    break;
                }
            }
            if (allKeysFalse) {
                //set all keys to true
                //search for the keys
                for (EntourageCategory category : categoryList) {
                    entourageCategories.add(category.getKey());
                }
            }
        }
    }

}
