package social.entourage.android.guide.poi

import android.content.Context
import android.graphics.Color
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.MarkerOptions
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.tools.utils.Utils

class PoiRenderer {
    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun getMarkerOptions(poi: Poi, context: Context): MarkerOptions? {
        val drawable = AppCompatResources.getDrawable(context, categoryForCategoryId(poi.categoryId).resourceId) ?: return null
        val icon = Utils.getBitmapDescriptorFromDrawable(drawable, BaseEntourage.getMarkerSize(context), BaseEntourage.getMarkerSize(context))
        return MarkerOptions()
                .position(poi.position)
                .title(poi.title)
                .icon(icon)
    }

    private fun categoryForCategoryId(categoryId: Int): CategoryType {
        return CategoryType.findCategoryTypeById(categoryId)
    }

    enum class CategoryType(val displayName: String, val categoryId: Int, val resourceId: Int, val color: Int, val filterId: Int) {
        OTHER("Other",
                0, R.drawable.ic_poi_cat_0_pin, Color.parseColor("#000000"), R.drawable.ic_poi_cat_0_round),
        FOOD("Se nourrir",
                1, R.drawable.ic_poi_cat_1_pin, Color.parseColor("#ffc57f"), R.drawable.ic_poi_cat_1_round),
        HOUSING("Se loger",
                2, R.drawable.ic_poi_cat_2_pin, Color.parseColor("#caa7ea"), R.drawable.ic_poi_cat_2_round),
        MEDICAL("Se soigner",
                3, R.drawable.ic_poi_cat_3_pin, Color.parseColor("#ff9999"), R.drawable.ic_poi_cat_3_round),
        ORIENTATION("S'orienter",
                5, R.drawable.ic_poi_cat_5_pin, Color.parseColor("#bfbfb9"), R.drawable.ic_poi_cat_5_round),
        INSERTION("Se réinsérer",
                7, R.drawable.ic_poi_cat_7_pin, Color.parseColor("#97d791"), R.drawable.ic_poi_cat_7_round),
        PARTNERS("Partenaires",
                8, R.drawable.ic_poi_cat_8_pin, Color.parseColor("#F99F7C"), R.drawable.ic_poi_cat_8_round),
        TOILETTES("Toilettes",
                40, R.drawable.ic_poi_cat_40_pin, Color.parseColor("#3ad7ff"), R.drawable.ic_poi_cat_40_round),
        FONTAINES("Fontaines",
                41, R.drawable.ic_poi_cat_41_pin, Color.parseColor("#3ad7ff"), R.drawable.ic_poi_cat_41_round),
        DOUCHES("Douches",
                42, R.drawable.ic_poi_cat_42_pin, Color.parseColor("#3ad7ff"), R.drawable.ic_poi_cat_42_round),
        LAVERLINGE("Laveries",
                43, R.drawable.ic_poi_cat_43_pin, Color.parseColor("#3ad7ff"), R.drawable.ic_poi_cat_43_round),
        SELF_CARE("Bien-être & activités",
                6, R.drawable.ic_poi_cat_6_pin, Color.parseColor("#88c0ff"), R.drawable.ic_poi_cat_6_round),
        VETEMENTS("Vêtements & matériels",
                61, R.drawable.ic_poi_cat_61_pin, Color.parseColor("#88c0ff"), R.drawable.ic_poi_cat_61_round),
        BAGAGES("Bagageries",
                63, R.drawable.ic_poi_cat_61_pin, Color.parseColor("#88c0ff"), R.drawable.ic_poi_cat_61_round),
        BOITESDONS("Boîtes à dons & lire",
                62, R.drawable.ic_poi_cat_62_pin, Color.parseColor("#88c0ff"), R.drawable.ic_poi_cat_62_round);

        companion object {
            fun findCategoryTypeById(categoryId: Int): CategoryType {
                for (categoryType in entries) {
                    if (categoryType.categoryId == categoryId) {
                        return categoryType
                    }
                }
                return OTHER
            }
        }

    }
}