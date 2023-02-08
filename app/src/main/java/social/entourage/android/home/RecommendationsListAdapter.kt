package social.entourage.android.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.databinding.NewRecommendationItemBinding
import social.entourage.android.api.model.HomeAction
import social.entourage.android.tools.utils.px
import timber.log.Timber

interface OnItemClickListener {
    fun onItemClick(recommendation: HomeAction)
}

class RecommendationsListAdapter(
    var recommendationsList: List<HomeAction>,
    var onItemClick: OnItemClickListener, var context: Context
) : RecyclerView.Adapter<RecommendationsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewRecommendationItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewRecommendationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        context = this.context
        with(holder) {
            with(recommendationsList[position]) {
                Glide.with(holder.itemView.context)
                    .load(this.imageURL)
                    .placeholder(R.drawable.new_illu_empty_state_event)
                    .into(binding.image)
                if(imageURL != null ){
                    binding.image.background = context.getDrawable(R.drawable.home_rounded_white)
                }
                binding.title.text = this.name
                binding.root.setOnClickListener {
                    onItemClick.onItemClick(this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return recommendationsList.size
    }
}
