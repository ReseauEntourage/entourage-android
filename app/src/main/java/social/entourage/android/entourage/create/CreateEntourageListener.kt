package social.entourage.android.entourage.create

import social.entourage.android.entourage.category.EntourageCategory

/**
 * Created by mihaiionescu on 18/05/2017.
 */
interface CreateEntourageListener {
    fun onTitleChanged(title: String)
    fun onDescriptionChanged(description: String)
    fun onCategoryChosen(category: EntourageCategory)
}