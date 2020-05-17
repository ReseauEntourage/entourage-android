package social.entourage.android.entourage.category

import androidx.annotation.StringRes
import com.google.gson.reflect.TypeToken
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */
object EntourageCategoryManager {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var defaultType: EntourageCategory? = null
    val entourageCategories = HashMap<String, List<EntourageCategory>>()
    private val defaultGroup: List<EntourageCategory>

    // ----------------------------------
    // Public methods
    // ----------------------------------
    fun getEntourageCategoriesForGroup(categoryGroup: String): List<EntourageCategory> {
        return entourageCategories[categoryGroup] ?: defaultGroup
    }

    fun findCategory(entourage: BaseEntourage): EntourageCategory? {
        return findCategory(entourage.actionGroupType, entourage.category)
    }

    @JvmStatic
    fun findCategory(entourageType: String, entourageCategory: String?): EntourageCategory? {
        val categoryToSearch = entourageCategory ?: "other"
        val list: List<EntourageCategory> = entourageCategories[entourageType] ?: return null
        for (category in list) {
            if (category.category != null && category.category.equals(categoryToSearch, ignoreCase = true)) {
                return category
            }
        }
        return null
    }

    @JvmStatic
    val defaultCategory: EntourageCategory
        get() = getDefaultCategory(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)

    @JvmStatic
    fun getDefaultCategory(groupType: String): EntourageCategory {
        var list: List<EntourageCategory>? = entourageCategories[groupType]
        if (list == null) {
            list = entourageCategories[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION] ?: defaultGroup
        }
        for (category in list) {
            if (category.isDefault) {
                return category
            }
        }
        return defaultType!!
    }

    @StringRes
    fun getGroupTypeDescription(groupType: String): Int {
        return when (groupType.toLowerCase()) {
            BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION -> R.string.entourage_category_type_contribution_label
            BaseEntourage.GROUPTYPE_ACTION_DEMAND -> R.string.entourage_category_type_demand_label
            else -> R.string.entourage_category_type_demand_label //By default : GROUPTYPE_DEMAND
        }
    }

    init {
        // Load our JSON file.
        val reader = JSONResourceReader(get().resources, R.raw.display_categories)
        val listType = object : TypeToken<ArrayList<EntourageCategory?>?>() {}.type
        val readEntourageCategories = reader.constructUsingGson<List<EntourageCategory>>(listType)
        val tempGroup: HashMap<String, MutableList<EntourageCategory>> = HashMap()
        for (category in readEntourageCategories) {
            val group = category.groupType ?: continue
            if (tempGroup[group] == null) {
                tempGroup[group] = ArrayList<EntourageCategory>(0).toMutableList()
            }
            tempGroup[group]!!.add(category)
            if (defaultType == null && category.isDefault) defaultType = category
        }
        entourageCategories[BaseEntourage.GROUPTYPE_ACTION_DEMAND] = tempGroup[BaseEntourage.GROUPTYPE_ACTION_DEMAND]!!
        defaultGroup = tempGroup[BaseEntourage.GROUPTYPE_ACTION_DEMAND]!!
        entourageCategories[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION] = tempGroup[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION]!!
    }
}