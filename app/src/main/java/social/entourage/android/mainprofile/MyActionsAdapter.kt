package social.entourage.android.mainprofile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_cell_my_action.view.*
import kotlinx.android.synthetic.main.layout_cell_my_action.view.ui_action_tv_location
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.category.EntourageCategoryManager
import java.util.ArrayList

/**
 * Created by Jerome on 14/10/2021.
 */
class MyActionsAdapter(var items: ArrayList<BaseEntourage>, val listenerClick: (position:Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(items: ArrayList<BaseEntourage>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyActionVH(layoutInflater.inflate(R.layout.layout_cell_my_action, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyActionVH).bind(items[position], listenerClick,position)
    }

    override fun getItemCount(): Int {
        return items.size
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
            itemView.ui_action_tv_info.text = cat.title

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
}