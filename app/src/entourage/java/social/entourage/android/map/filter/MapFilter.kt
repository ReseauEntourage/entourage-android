package social.entourage.android.map.filter

import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.TourType
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import java.io.Serializable
import java.util.*

/**
 * Created by mihaiionescu on 17/05/16.
 */
class MapFilter : MapFilterInterface, Serializable {
    var tourTypeMedical = false
    var tourTypeSocial = false
    var tourTypeDistributive = false
    var entourageTypeOuting = true
    var showPastEvents = false
    var entourageTypeDemand = true
    var entourageTypeContribution = true
    var showTours = false
    var timeframe = DAYS_3

    private var entourageCategories: MutableList<String> = ArrayList()

    // ----------------------------------
    // MapFilter implementation
    // ----------------------------------
    override fun getTypes(): String {
        val entourageTypes = StringBuilder()
        if (tourTypeMedical) {
            entourageTypes.append(TourType.MEDICAL.key)
        }
        if (tourTypeSocial) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append(TourType.BARE_HANDS.key)
        }
        if (tourTypeDistributive) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append(TourType.ALIMENTARY.key)
        }
        if (entourageTypeOuting) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append("ou")
        }
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
        if (entourageTypeDemand) validateCategoriesForType(Entourage.TYPE_DEMAND)
        if (entourageTypeContribution) validateCategoriesForType(Entourage.TYPE_CONTRIBUTION)
    }

    override fun isDefaultFilter(): Boolean{
        var isProUser:Boolean? = EntourageApplication.get()?.entourageComponent?.authenticationController?.user?.isPro
        if(isProUser == null) isProUser = false
        return when {
            !tourTypeMedical && isProUser -> false
            !tourTypeSocial && isProUser -> false
            !tourTypeDistributive && isProUser -> false
            !entourageTypeDemand && !isProUser -> false
            !entourageTypeContribution && !isProUser -> false
            !showTours && isProUser -> false
            //normal filter
            !entourageTypeOuting -> false
            //TODO check tis one: //showPastEvents -> false
            timeframe != DAYS_3 -> false
            else -> true
        }
    }

    fun setDefaultValues(isProUser: Boolean) {
        //var isProUser:Boolean? = EntourageApplication.get()?.entourageComponent?.authenticationController?.user?.isPro
        //if(isProUser==null) isProUser = false;
        tourTypeMedical = isProUser
        tourTypeSocial = isProUser
        tourTypeDistributive = isProUser
        entourageTypeDemand = !isProUser
        entourageTypeContribution = !isProUser
        showTours = isProUser
        entourageTypeOuting=true
        showPastEvents=false
        timeframe = DAYS_3
    }

    // ----------------------------------
    // Methods
    // ----------------------------------
    fun isCategoryChecked(entourageCategory: EntourageCategory): Boolean {
        if (Entourage.TYPE_DEMAND == entourageCategory.entourageType) return entourageTypeDemand && entourageCategories.contains(entourageCategory.key)
        return if (Entourage.TYPE_CONTRIBUTION == entourageCategory.entourageType) entourageTypeContribution && entourageCategories.contains(entourageCategory.key) else false
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

    // ----------------------------------
    // Serialization
    // ----------------------------------
    private fun validateCategoriesForType(actionType: String?) {
        //get the list of categories for this type
        val categoryList = EntourageCategoryManager.getInstance().getEntourageCategoriesForType(actionType)
        if (categoryList != null) {
            var allKeysFalse = true
            //search for the keys
            for(category in categoryList) {
                if (entourageCategories.contains(category.key)) {
                    allKeysFalse = false
                    break
                }
            }
            if (allKeysFalse) {
                //set all keys to true
                //search for the keys
                for (category in categoryList) {
                    entourageCategories.add(category.key)
                }
            }
        }
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