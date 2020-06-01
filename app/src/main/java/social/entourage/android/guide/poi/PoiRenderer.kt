package social.entourage.android.guide.poi

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.guide.Category

class PoiRenderer(context: Context?, map: GoogleMap?, clusterManager: ClusterManager<Poi>?) : DefaultClusterRenderer<Poi>(context, map, clusterManager) {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var categories: List<Category>? = null

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    override fun onBeforeClusterItemRendered(poi: Poi, markerOptions: MarkerOptions) {
        val categoryType = categoryForCategoryId(poi.categoryId)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(categoryType.resourceId))
    }

    private fun categoryForCategoryId(categoryId: Int): CategoryType {
        categories?.let {
            for (category in it) {
                if (category.id == categoryId.toLong()) {
                    return CategoryType.findCategoryTypeByName(category.name)
                }
            }
            return CategoryType.OTHER
        } ?: return CategoryType.findCategoryTypeById(categoryId)
    }

    fun setCategories(categories: List<Category>) {
        this.categories = categories
    }

    enum class CategoryType(val displayName: String, val categoryId: Int, val resourceId: Int, val resourceTransparentId: Int, val color: Int) {
        OTHER("Other", 0, 0, 0, Color.parseColor("#000000")),
        FOOD("Se nourrir", 1, R.drawable.poi_category_1, R.drawable.poi_transparent_category_1, Color.parseColor("#ffc57f")),
        HOUSING("Se loger", 2, R.drawable.poi_category_2, R.drawable.poi_transparent_category_2, Color.parseColor("#caa7ea")),
        MEDICAL("Se soigner", 3, R.drawable.poi_category_3, R.drawable.poi_transparent_category_3, Color.parseColor("#ff9999")),
        WATER("Se rafraîchir", 4, R.drawable.poi_category_4, R.drawable.poi_transparent_category_4, Color.parseColor("#3ad7ff")),
        ORIENTATION("S'orienter", 5, R.drawable.poi_category_5, R.drawable.poi_transparent_category_5, Color.parseColor("#bfbfb9")),
        SELF_CARE("S'occuper de soi", 6, R.drawable.poi_category_6, R.drawable.poi_transparent_category_6, Color.parseColor("#88c0ff")),
        INSERTION("Se réinsérer", 7, R.drawable.poi_category_7, R.drawable.poi_transparent_category_7, Color.parseColor("#97d791"));

        companion object {
            fun findCategoryTypeByName(name: String?): CategoryType {
                for (categoryType in values()) {
                    if (categoryType.displayName.equals(name, ignoreCase = true)) {
                        return categoryType
                    }
                }
                return OTHER
            }

            fun findCategoryTypeById(categoryId: Int): CategoryType {
                for (categoryType in values()) {
                    if (categoryType.categoryId == categoryId) {
                        return categoryType
                    }
                }
                return OTHER
            }
        }

    }
}