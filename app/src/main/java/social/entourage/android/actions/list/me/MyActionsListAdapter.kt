package social.entourage.android.actions.list.me

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.databinding.LayoutMyActionItemBinding
import social.entourage.android.tools.utils.px

interface OnItemClick {
    fun onItemClick(action: Action)
}

class MyActionsListAdapter(
    private var groupsList: List<Action>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<MyActionsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyActionsListAdapter.ViewHolder {
        val view = LayoutMyActionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val binding: LayoutMyActionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(action: Action) {

            binding.layout.setOnClickListener {
                onItemClickListener.onItemClick(action)
            }

            binding.name.text = action.title
            binding.date.text = action.dateFormattedString(binding.root.context)

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

    override fun onBindViewHolder(holder: MyActionsListAdapter.ViewHolder, position: Int) {
        holder.bind(groupsList[position])
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}
