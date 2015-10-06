package social.entourage.android.guide;

import android.content.Context;

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
        BitmapDescriptor poiIcon = BitmapDescriptorFactory.fromResource(categoryType.getRessourceId());
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
        return CategoryType.OTHER;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    private enum CategoryType {
        INSERTION("Se réinsérer", R.drawable.poi_category_1),
        SELF_CARE("S'occuper de soi", R.drawable.poi_category_2),
        ORIENTATION("S'orienter", R.drawable.poi_category_3),
        WATER("Se rafraîchir", R.drawable.poi_category_4),
        MEDICAL("Se soigner", R.drawable.poi_category_5),
        HOUSING("Se loger", R.drawable.poi_category_6),
        FOOD("Se nourrir", R.drawable.poi_category_7),
        OTHER("Other", 0);

        private final String name;
        private final int ressourceId;

        CategoryType(String name, int ressourceId) {
            this.name = name;
            this.ressourceId = ressourceId;
        }

        public int getRessourceId() {
            return ressourceId;
        }

        public static CategoryType findCategoryTypeByName(String name) {
            for(CategoryType categoryType : values()) {
                if (categoryType.name.equalsIgnoreCase(name)) {
                    return categoryType;
                }
            }
            return OTHER;
        }
    }
}
