package social.entourage.android.mainprofile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_cell_my_action.view.*
import kotlinx.android.synthetic.main.layout_cell_my_action.view.ui_action_tv_location
import kotlinx.android.synthetic.main.layout_cell_my_action.view.ui_action_tv_title
import kotlinx.android.synthetic.main.layout_cell_my_action_empty.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.category.EntourageCategoryManager
import java.util.ArrayList

/**
 * Created by Jerome on 14/10/2021.
 */
class MyActionsAdapter(var items: ArrayList<BaseEntourage>, var isContrib:Boolean,var isFirstLoad:Boolean, val listenerClick: (position:Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val CELL_EMPTY = 0
    val CELL = 1

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(items: ArrayList<BaseEntourage>,isContrib:Boolean,isFirstLoad:Boolean) {
        this.items = items
        this.isContrib = isContrib
        this.isFirstLoad = isFirstLoad
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (items.size == 0) return CELL_EMPTY
        return CELL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (viewType == CELL_EMPTY) {
            return MyActionEmptyVH(layoutInflater.inflate(R.layout.layout_cell_my_action_empty, parent, false))
        }
        return MyActionVH(layoutInflater.inflate(R.layout.layout_cell_my_action, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == CELL_EMPTY) {
            (holder as MyActionEmptyVH).bind(this.isContrib)
            return
        }
        (holder as MyActionVH).bind(items[position], listenerClick,position)
    }

    override fun getItemCount(): Int {
        if (isFirstLoad) return 0
        return if(items.size == 0) 1 else items.size
    }

    inner class MyActionVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(feedItem: BaseEntourage, listener: (position:Int) -> Unit, position: Int) {
            itemView.setOnClickListener {
                listener(position)
            }

            val res = itemView.resources

            itemView.ui_action_tv_title?.let { titleView ->
                titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle())
            }

            //Info
            val cat = EntourageCategoryManager.findCategory(feedItem)
            itemView.ui_action_tv_info.text = cat.title_list

            itemView.ui_action_picto_info?.let { iconView ->
                Glide.with(iconView.context).clear(iconView)
                feedItem.getIconURL()?.let { iconURL ->
                    iconView.setImageDrawable(null)
                    Glide.with(iconView.context)
                            .load(iconURL)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .circleCrop()
                            .into(iconView)
                } ?: run {
                    iconView.setImageDrawable(feedItem.getIconDrawable(itemView.context))
                }
            }

            //Location
            val distanceAsString = feedItem.getStartPoint()?.distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
                    ?: ""
            var distStr = if (distanceAsString.equals("", ignoreCase = true)) "" else String.format(res.getString(R.string.tour_cell_location), distanceAsString)

            feedItem.postal_code?.let { postalCode ->
                if (distStr.isNotEmpty() && postalCode.isNotEmpty()) {
                    distStr = "%s - %s".format(distStr, postalCode)
                } else if (postalCode.isNotEmpty()) {
                    distStr = postalCode
                }
            }

            itemView.ui_action_tv_location?.text = distStr
        }
    }

    inner class MyActionEmptyVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(isContrib:Boolean) {
            val res = itemView.resources
            itemView.ui_tv_title?.let { titleView ->
                titleView.text = if(isContrib) res.getString(R.string.myActionEmptyContrib) else res.getString(R.string.myActionEmptyAsk)
            }
        }
    }
}