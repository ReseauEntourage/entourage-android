package social.entourage.android.guide.filter;

import java.io.Serializable;
import java.util.HashMap;

import social.entourage.android.guide.PoiRenderer;

/**
 * Created by mihaiionescu on 28/03/2017.
 */

public class GuideFilter implements Serializable {

    private static final long serialVersionUID = -5047709875896955208L;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public HashMap<Integer, Boolean> filterValues = new HashMap<>();

    private static GuideFilter instance = new GuideFilter();

    private GuideFilter() {
        for (PoiRenderer.CategoryType categoryType: PoiRenderer.CategoryType.values()
             ) {
            filterValues.put(categoryType.getCategoryId(), true);
        }
    }

    public static GuideFilter getInstance() {
        return instance;
    }

    public boolean valueForCategoryId(int categoryId) {
        return filterValues.get(categoryId);
    }

    public void setValueForCategoryId(int categoryId, boolean value) {
        filterValues.put(categoryId, value);
    }

}
