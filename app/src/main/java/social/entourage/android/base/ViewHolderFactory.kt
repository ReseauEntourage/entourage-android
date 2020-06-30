package social.entourage.android.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import java.util.*

/**
 * Created by mihaiionescu on 02/03/16.
 */
class ViewHolderFactory {
    private val viewHolderTypeHashMap: HashMap<Int, ViewHolderType> = HashMap()
    private var viewHolderTypeDefault: ViewHolderType? = null

    fun registerViewHolder(viewType: Int, viewHolderType: ViewHolderType) {
        if (viewHolderTypeHashMap.isEmpty()) {
            viewHolderTypeDefault = viewHolderType
        }
        viewHolderTypeHashMap[viewType] = viewHolderType
    }

    fun getViewHolder(parent: ViewGroup, viewType: Int): BaseCardViewHolder {
        val viewHolderType = viewHolderTypeHashMap[viewType] ?:  viewHolderTypeDefault

        val view = LayoutInflater.from(parent.context).inflate(viewHolderType!!.layoutResource, parent, false)

        var cardViewHolder: BaseCardViewHolder? = null
        try {
            val ctor = viewHolderType.cardViewHolderClass.getConstructor(View::class.java)
            cardViewHolder = ctor.newInstance(view) as BaseCardViewHolder
        } catch (e: Exception) {
            Timber.e(e)
        }
        return cardViewHolder!!
    }

    class ViewHolderType(val cardViewHolderClass: Class<*>, val layoutResource: Int)

}