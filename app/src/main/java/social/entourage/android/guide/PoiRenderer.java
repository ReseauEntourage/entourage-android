package social.entourage.android.guide;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Poi;

public class PoiRenderer extends DefaultClusterRenderer<Poi> {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private List<Category> categories;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public PoiRenderer(Context context, GoogleMap map, ClusterManager<Poi> clusterManager) {
        super(context, map, clusterManager);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Override
    protected void onBeforeClusterItemRendered(Poi poi, MarkerOptions markerOptions) {
        CategoryType categoryType = categoryForCategoryId(poi.getCategoryId());
        BitmapDescriptor poiIcon = BitmapDescriptorFactory.fromResource(categoryType.getResourceId());
        markerOptions.icon(poiIcon);
    }

    CategoryType categoryForCategoryId(int categoryId) {
        if (categories != null) {
            for (Category category: categories) {
                if (category.getId() == categoryId) {
                    return CategoryType.findCategoryTypeByName(category.getName());
                }
            }
        }
        else {
            return CategoryType.findCategoryTypeById(categoryId);
        }
        return CategoryType.OTHER;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public enum CategoryType {

        OTHER("Other", 0, 0, 0, Color.parseColor("#000000")),
        FOOD("Se nourrir", 1, R.drawable.poi_category_1, R.drawable.poi_transparent_category_1, Color.parseColor("#ffc57f")),
        HOUSING("Se loger", 2, R.drawable.poi_category_2, R.drawable.poi_transparent_category_2, Color.parseColor("#caa7ea")),
        MEDICAL("Se soigner", 3, R.drawable.poi_category_3, R.drawable.poi_transparent_category_3, Color.parseColor("#ff9999")),
        WATER("Se rafraîchir", 4, R.drawable.poi_category_4, R.drawable.poi_transparent_category_4, Color.parseColor("#3ad7ff")),
        ORIENTATION("S'orienter", 5,  R.drawable.poi_category_5, R.drawable.poi_transparent_category_5, Color.parseColor("#bfbfb9")),
        SELF_CARE("S'occuper de soi", 6,  R.drawable.poi_category_6, R.drawable.poi_transparent_category_6, Color.parseColor("#88c0ff")),
        INSERTION("Se réinsérer", 7, R.drawable.poi_category_7, R.drawable.poi_transparent_category_7, Color.parseColor("#97d791"));

        private final String name;
        private final int categoryId;
        private final int resourceId;
        private final int resourceTransparentId;
        private final int color;

        CategoryType(String name, int categoryId, int resourceId, int resourceTransparentId, int color) {
            this.name = name;
            this.categoryId = categoryId;
            this.resourceId = resourceId;
            this.resourceTransparentId = resourceTransparentId;
            this.color = color;
        }

        public int getResourceId() {
            return resourceId;
        }

        public int getResourceTransparentId() {
            return resourceTransparentId;
        }

        public int getColor() {
            return color;
        }

        public String getName() {
            return name;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public static CategoryType findCategoryTypeByName(String name) {
            for(CategoryType categoryType : values()) {
                if (categoryType.name.equalsIgnoreCase(name)) {
                    return categoryType;
                }
            }
            return OTHER;
        }

        public static CategoryType findCategoryTypeById(int categoryId) {
            for(CategoryType categoryType : values()) {
                if (categoryType.categoryId == categoryId) {
                    return categoryType;
                }
            }
            return OTHER;
        }
    }
}
