package social.entourage.android.guide.poi

import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import social.entourage.android.R
import social.entourage.android.api.model.guide.Category
import social.entourage.android.api.model.guide.Poi

class PoiRenderer {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var categories: List<Category>? = null

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun getMarkerOptions(poi: Poi): MarkerOptions {
        return MarkerOptions()
                .position(poi.position)
                .title(poi.title)
                .icon(BitmapDescriptorFactory.fromResource(categoryForCategoryId(poi.categoryId).resourceId))
    }

    private fun categoryForCategoryId(categoryId: Int): CategoryType {
        categories?.let {
            for (category in it) {
                if (category.id == categoryId.toLong()) {
                    return CategoryType.findCategoryTypeByName(category.name)
                }
            }
            return CategoryType.OTHER
        }
        return CategoryType.findCategoryTypeById(categoryId)
    }

    fun setCategories(categories: List<Category>) {
        this.categories = categories
    }

    enum class CategoryType(val displayName: String, val categoryId: Int, val resourceId: Int, val color: Int, val filterId: Int) {
        OTHER("Other",
                0, R.drawable.poi_category_new_0, Color.parseColor("#000000"), R.drawable.picto_cat_filter_0),
        FOOD("Se nourrir",
                1, R.drawable.poi_category_new_1, Color.parseColor("#ffc57f"), R.drawable.picto_cat_filter_1),
        HOUSING("Se loger",
                2, R.drawable.poi_category_new_2, Color.parseColor("#caa7ea"), R.drawable.picto_cat_filter_2),
        MEDICAL("Se soigner",
                3, R.drawable.poi_category_new_3, Color.parseColor("#ff9999"), R.drawable.picto_cat_filter_3),
        ORIENTATION("S'orienter",
                5, R.drawable.poi_category_new_5, Color.parseColor("#bfbfb9"), R.drawable.picto_cat_filter_5),
        INSERTION("Se réinsérer",
                7, R.drawable.poi_category_new_7, Color.parseColor("#97d791"), R.drawable.picto_cat_filter_7),
        PARTNERS("Partenaires",
                8, R.drawable.poi_category_new_8, Color.parseColor("#F99F7C"), R.drawable.picto_cat_filter_8),
        TOILETTES("Toilettes",
                40, R.drawable.poi_category_new_40, Color.parseColor("#3ad7ff"), R.drawable.picto_cat_filter_40),
        FONTAINES("Fontaines",
                41, R.drawable.poi_category_new_41, Color.parseColor("#3ad7ff"), R.drawable.picto_cat_filter_41),
        DOUCHES("Douches",
                42, R.drawable.poi_category_new_42, Color.parseColor("#3ad7ff"), R.drawable.picto_cat_filter_42),
        LAVERLINGE("Laveries",
                43, R.drawable.poi_category_new_43, Color.parseColor("#3ad7ff"), R.drawable.picto_cat_filter_43),
        SELF_CARE("Bien-être & activités",
                6, R.drawable.poi_category_new_6, Color.parseColor("#88c0ff"), R.drawable.picto_cat_filter_6),
        VETEMENTS("Vêtements & matériels",
                61, R.drawable.poi_category_new_61, Color.parseColor("#88c0ff"), R.drawable.picto_cat_filter_61),
        BAGAGES("Bagageries",
                63, R.drawable.poi_category_new_63, Color.parseColor("#88c0ff"), R.drawable.picto_cat_filter_63),
        BOITESDONS("Boîtes à dons & lire",
                62, R.drawable.poi_category_new_62, Color.parseColor("#88c0ff"), R.drawable.picto_cat_filter_62);

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