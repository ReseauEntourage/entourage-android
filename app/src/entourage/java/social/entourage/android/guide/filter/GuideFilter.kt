package social.entourage.android.guide.filter

import android.util.SparseBooleanArray
import social.entourage.android.guide.poi.PoiRenderer.CategoryType
import java.io.Serializable

/**
 * Created by mihaiionescu on 28/03/2017.
 */
class GuideFilter private constructor() : Serializable {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private val filterValues = SparseBooleanArray()

    fun valueForCategoryId(categoryId: Int): Boolean {
        return filterValues[categoryId]
    }

    fun setValueForCategoryId(categoryId: Int, value: Boolean) {
        filterValues.put(categoryId, value)
    }

    /**
     * Retrieve the requested categories (those not filtered out)
     *
     * @return comma separated category ids if there is a filter, otherwise null
     */
    val requestedCategories: String?
        get() {
            val builder = StringBuilder()
            var existingFilteredCategories = false
            for (categoryType in CategoryType.values()) {
                if (valueForCategoryId(categoryType.categoryId)) {
                    if (builder.isNotEmpty()) builder.append(',')
                    builder.append(categoryType.categoryId)
                } else {
                    existingFilteredCategories = true
                }
            }
            return if (existingFilteredCategories) builder.toString() else null
        }

    /**
     * Tells if there is any category filtered out
     *
     * @return true if any category is filtered out, false otherwise
     */
    fun hasFilteredCategories(): Boolean {
        for (catType in CategoryType.values()) {
            if (!valueForCategoryId(catType.categoryId)) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val serialVersionUID = -5047709875896955208L
        @JvmStatic
        val instance = GuideFilter()
    }

    init {
        for (categoryType in CategoryType.values()) {
            filterValues.put(categoryType.categoryId, true)
        }
    }
}