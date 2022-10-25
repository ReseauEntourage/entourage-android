package social.entourage.android.new_v8.actions.list.me

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.new_my_action_item.view.*
import social.entourage.android.R
import social.entourage.android.new_v8.models.*
import social.entourage.android.new_v8.utils.px


interface OnItemClick {
    fun onItemClick(action: Action)
}

class MyActionsListAdapter(
    var groupsList: List<Action>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_my_action_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(action: Action) {

            binding.layout.setOnClickListener {
                onItemClickListener.onItemClick(action)
            }

            binding.name.text = action.title
            binding.date.text = action.dateFormattedString(binding.context)

            action.imageUrl?.let {
                Glide.with(binding.image.context)
                    .load(it)
                    .placeholder(R.drawable.ic_placeholder_action)
                    .error(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            } ?: run {
                Glide.with(binding.image.context)
                    .load(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.bind(groupsList[position])
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}
