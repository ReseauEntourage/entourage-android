package social.entourage.android.entourage.category

import androidx.annotation.StringRes
import com.google.gson.reflect.TypeToken
import social.entourage.android.EntourageApplication
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
    private lateinit var defaultType: EntourageCategory
    private lateinit var unknownType: EntourageCategory
    val entourageCategories = HashMap<String, List<EntourageCategory>>()
    private val defaultGroup: List<EntourageCategory>

    // ----------------------------------
    // Public methods
    // ----------------------------------
    fun getEntourageCategoriesForGroup(categoryGroup: String): List<EntourageCategory> {
        return entourageCategories[categoryGroup] ?: defaultGroup
    }

    fun findCategory(entourage: BaseEntourage): EntourageCategory {
        return findCategory(entourage.actionGroupType, entourage.category)
    }

    fun findCategory(entourageType: String, entourageCategory: String?): EntourageCategory {
        val categoryToSearch = entourageCategory ?: EntourageCategory.CATEGORY_UNKNOWN
        val list: List<EntourageCategory> = getEntourageCategoriesForGroup(entourageType)
        list.forEach { category ->
            if (categoryToSearch.equals(category.category, ignoreCase = true)) {
                return category
            }
        }
        return unknownType
    }

    val defaultCategory: EntourageCategory
        get() = getDefaultCategory(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)

    fun getDefaultCategory(groupType: String): EntourageCategory {
        val list: List<EntourageCategory> = getEntourageCategoriesForGroup(groupType)
        for (category in list) {
            if (category.isDefault) {
                return category
            }
        }
        return defaultType
    }

    @StringRes
    fun getGroupTypeDescription(groupType: String): Int {
        return when (groupType.lowercase(Locale.ROOT)) {
            BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION -> R.string.entourage_category_type_contribution_label
            BaseEntourage.GROUPTYPE_ACTION_DEMAND -> R.string.entourage_category_type_demand_label
            else -> R.string.entourage_category_type_demand_label //By default : GROUPTYPE_DEMAND
        }
    }

    init {
        // Load our JSON file.
        val reader =
            JSONResourceReader(EntourageApplication.get().resources, R.raw.display_categories)
        val listType = object : TypeToken<ArrayList<EntourageCategory?>?>() {}.type
        val readEntourageCategories = reader.constructUsingGson<List<EntourageCategory>>(listType)
        val tempGroup: HashMap<String, MutableList<EntourageCategory>> = HashMap()
        for (category in readEntourageCategories) {
            val group = category.groupType ?: continue
            if (tempGroup[group] == null) {
                tempGroup[group] = ArrayList<EntourageCategory>(0).toMutableList()
            }
            tempGroup[group]?.add(category)
            if (!this::defaultType.isInitialized && category.isDefault) defaultType = category
            if (!this::unknownType.isInitialized && EntourageCategory.CATEGORY_UNKNOWN.equals(category.category, false)) unknownType = category
        }
        tempGroup[BaseEntourage.GROUPTYPE_ACTION_DEMAND]?.let{
            entourageCategories[BaseEntourage.GROUPTYPE_ACTION_DEMAND] = it
        }
        tempGroup[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION]?.let {
            entourageCategories[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION] = it
        }
        //TODO make sure we have this category
        defaultGroup = entourageCategories[BaseEntourage.GROUPTYPE_ACTION_DEMAND] ?: emptyList()
    }
}