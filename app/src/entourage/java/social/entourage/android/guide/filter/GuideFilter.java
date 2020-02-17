package social.entourage.android.guide.filter;

import android.util.SparseBooleanArray;

import org.jetbrains.annotations.NotNull;

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

    private SparseBooleanArray filterValues = new SparseBooleanArray();

    private static GuideFilter instance = new GuideFilter();

    private GuideFilter() {
        for (PoiRenderer.CategoryType categoryType: PoiRenderer.CategoryType.values()) {
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

    /**
     * Retrieve the requested categories (those not filtered out)
     *
     * @return comma separated category ids if there is a filter, otherwise null
     */
    public String getRequestedCategories() {
        StringBuilder builder = new StringBuilder();
        boolean existingFilteredCategories = false;
        for (PoiRenderer.CategoryType categoryType: PoiRenderer.CategoryType.values()) {
            if (valueForCategoryId(categoryType.getCategoryId())) {
                if (builder.length() > 0) builder.append(',');
                builder.append(categoryType.getCategoryId());
            } else {
                existingFilteredCategories = true;
            }
        }
        if (existingFilteredCategories) return builder.toString();
        return null;
    }


    /**
     * Tells if there is any category filtered out
     *
     * @return true if any category is filtered out, false otherwise
     */
    public boolean hasFilteredCategories() {
        for (PoiRenderer.CategoryType catType: PoiRenderer.CategoryType.values()) {
            if (!valueForCategoryId(catType.getCategoryId())) {
                return true;
            }
        }
        return false;
    }

}
