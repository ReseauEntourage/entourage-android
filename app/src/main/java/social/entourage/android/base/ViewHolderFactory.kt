package social.entourage.android.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by mihaiionescu on 02/03/16.
 */
class ViewHolderFactory {
    private val viewHolderTypeHashMap: HashMap<Int, ViewHolderType> = HashMap()
    private lateinit var viewHolderTypeDefault: ViewHolderType

    fun registerViewHolder(viewType: Int, viewHolderType: ViewHolderType) {
        if (viewHolderTypeHashMap.isEmpty()) {
            viewHolderTypeDefault = viewHolderType
        }
        viewHolderTypeHashMap[viewType] = viewHolderType
    }

    fun getViewHolder(parent: ViewGroup, viewType: Int): BaseCardViewHolder {
        val viewHolderType = viewHolderTypeHashMap[viewType] ?:  viewHolderTypeDefault

        val view = LayoutInflater.from(parent.context).inflate(viewHolderType.layoutResource, parent, false)

        return viewHolderType.cardViewHolderClass.getConstructor(View::class.java).newInstance(view) as BaseCardViewHolder
    }

    class ViewHolderType(val cardViewHolderClass: Class<*>, val layoutResource: Int)

}