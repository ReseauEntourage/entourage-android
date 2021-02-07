package social.entourage.android.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.TimestampedObject

/**
 * Created by mihaiionescu on 02/03/16.
 */
abstract class BaseCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var viewHolderListener: BaseViewHolderListener? = null

    protected abstract fun bindFields()
    abstract fun populate(data: TimestampedObject)

    init {
        bindFields()
    }
}