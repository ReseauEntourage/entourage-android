package social.entourage.android.base.map.filter

import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.category.EntourageCategory
import java.io.Serializable
import java.util.ArrayList

/**
 * Created by mihaiionescu on 17/05/16.
 */
class MapFilter : MapFilterInterface, Serializable {
    var entourageTypeOuting = true
    var showPastEvents = false
    var entourageTypeDemand = true
    var entourageTypeContribution = true
    var timeframe = DAYS_3

    var isShowAlls = true
    var isShowPartnersOnly = false

    private var entourageCategories: MutableList<String> = ArrayList()

    // ----------------------------------
    // MapFilter implementation
    // ----------------------------------
    override fun getTypes(): String {
        val entourageTypes = StringBuilder()
        for (categoryKey in entourageCategories) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append(categoryKey)
        }
        return entourageTypes.toString()
    }

    override fun getTimeFrame(): Int {
        return timeframe
    }

    override fun showPastEvents(): Boolean {
        return showPastEvents
    }

    override fun entourageCreated() {
        entourageTypeContribution = true
        entourageTypeDemand = true
    }

    override fun validateCategories() {
        if (entourageTypeDemand) validateCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_DEMAND)
        if (entourageTypeContribution) validateCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
    }

    override fun isDefaultFilter(): Boolean{
        return when {
            !entourageTypeDemand  -> false
            !entourageTypeContribution -> false
            //normal filter
            entourageTypeOuting -> false
            //!entourageTypeOuting -> false Old version
            //TODO check tis one: //showPastEvents -> false
            timeframe != DAYS_3 -> false
            isShowPartnersOnly -> false
            else -> true
        }
    }

    override fun setDefaultValues() {
        entourageTypeDemand = true
        entourageTypeContribution = true
        showPastEvents=false
        timeframe = DAYS_3
        isShowPartnersOnly = false
        isShowAlls = true
        entourageTypeOuting = false

        setAllCategorySelected(true,true)
    }

    fun setAllCategorySelected(contribActive:Boolean,demandActive:Boolean) {
        val entourageCategoryList =
            EntourageCategoryManager.getEntourageCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)

        for (entourageCategory in entourageCategoryList) {
            entourageCategory.key?.let {
                setCategoryChecked(entourageCategory.key!!, contribActive)
            }
        }

        val entourageCategoryList2 =
            EntourageCategoryManager.getEntourageCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_DEMAND)

        for (entourageCategory in entourageCategoryList2) {
            entourageCategory.key?.let {
                setCategoryChecked(entourageCategory.key!!, demandActive)
            }
        }
    }

    // ----------------------------------
    // Methods
    // ----------------------------------
    fun isCategoryChecked(actionGroupType: EntourageCategory): Boolean {
        return when(actionGroupType.groupType) {
            BaseEntourage.GROUPTYPE_ACTION_DEMAND -> {
                entourageTypeDemand && entourageCategories.contains(actionGroupType.key)
            }
            BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION -> {
                entourageTypeContribution && entourageCategories.contains(actionGroupType.key)
            }
            else -> false
        }
    }

    fun setCategoryChecked(actionCategory: String, checked: Boolean) {
        if (checked) {
            if (!entourageCategories.contains(actionCategory)) {
                entourageCategories.add(actionCategory)
            }
        } else {
            entourageCategories.remove(actionCategory)
        }
    }

    //Setup Filters for specific user type
    /*fun setNeighbourFilters() {
        entourageTypeDemand = true
        entourageTypeContribution = false
        entourageTypeOuting = true
        timeframe = DAYS_3
        showPastEvents = false
        //Remove contrib types
        val entourageCategoryList = EntourageCategoryManager.getEntourageCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)

        for (entourageCategory in entourageCategoryList) {
            entourageCategory.key?.let {
                setCategoryChecked(entourageCategory.key!!, false)
            }
        }
    }*/

   /* fun setAloneFilters() {
        entourageTypeDemand = false
        entourageTypeContribution = true
        entourageTypeOuting = true
        timeframe = DAYS_3
        showPastEvents = false
        //Remove demand types
        val entourageCategoryList = EntourageCategoryManager.getEntourageCategoriesForGroup(BaseEntourage.GROUPTYPE_ACTION_DEMAND)

        for (entourageCategory in entourageCategoryList) {
            entourageCategory.key?.let {
                setCategoryChecked(entourageCategory.key!!, false)
            }
        }
    }*/

    // ----------------------------------
    // Serialization
    // ----------------------------------
    private fun validateCategoriesForGroup(actionGroup: String) {
        //get the list of categories for this type
        val categoryList = EntourageCategoryManager.getEntourageCategoriesForGroup(actionGroup)
        //search for the keys
        for(category in categoryList) {
            if (entourageCategories.contains(category.key)) {
                //allKeysFalse = false
                return
            }
        }
        //set all keys to true
        //search for the keys
        categoryList.forEach {it.key?.let {key -> entourageCategories.add(key)} }
    }

    companion object {
        private const val serialVersionUID = -2822136342813499636L

        // ----------------------------------
        // Attributes
        // ----------------------------------
        const val DAYS_1 = 24 //hours
        const val DAYS_2 = 8 * 24 //hours
        const val DAYS_3 = 30 * 24 //hours
    }

    init {
        validateCategories()
    }
}