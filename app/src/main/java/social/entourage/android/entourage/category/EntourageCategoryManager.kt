package social.entourage.android.entourage.category

import androidx.annotation.StringRes
import com.google.gson.reflect.TypeToken
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.map.BaseEntourage
import java.util.*

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */
object EntourageCategoryManager {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @JvmStatic
    var groupTypes: MutableList<String?> = ArrayList()
        private set
    private var defaultType: EntourageCategory? = null
    @JvmStatic
    val entourageCategories = HashMap<String?, MutableList<EntourageCategory>>()
    private val defaultGroup: MutableList<EntourageCategory>

    // ----------------------------------
    // Public methods
    // ----------------------------------
    fun getEntourageCategoriesForGroup(categoryGroup: String): List<EntourageCategory> {
        return entourageCategories[categoryGroup] ?: defaultGroup
    }

    fun findCategory(entourage: BaseEntourage?): EntourageCategory? {
        return if (entourage == null) null else findCategory(entourage.actionGroupType, entourage.category)
    }

    @JvmStatic
    fun findCategory(entourageType: String?, entourageCategory: String?): EntourageCategory? {
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
    val defaultCategory: EntourageCategory?
        get() = getDefaultCategory(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)

    @JvmStatic
    fun getDefaultCategory(groupType: String?): EntourageCategory {
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

    @JvmStatic
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
        // To preserve the required order, we add the know types in the required order and the other types will get added after them
        groupTypes.add(BaseEntourage.GROUPTYPE_ACTION_DEMAND)
        groupTypes.add(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
        // Construct the hashmap
        defaultGroup = ArrayList()
        entourageCategories[BaseEntourage.GROUPTYPE_ACTION_DEMAND] = defaultGroup
        entourageCategories[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION] = ArrayList<EntourageCategory>()
        for (category in readEntourageCategories) {
            val group = category.groupType
            var list: MutableList<EntourageCategory>? = entourageCategories.get(group)
            if (list == null) {
                groupTypes.add(group)
                list = ArrayList()
                entourageCategories[group] = list as MutableList<EntourageCategory>
            }
            list!!.add(category)
            if (defaultType == null && category.isDefault) defaultType = category
        }
    }
}