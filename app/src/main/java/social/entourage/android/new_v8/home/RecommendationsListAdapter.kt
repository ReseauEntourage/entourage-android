package social.entourage.android.new_v8.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.databinding.NewRecommendationItemBinding
import social.entourage.android.new_v8.models.Recommandation
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px

interface OnItemClickListener {
    fun onItemClick(recommendation: Recommandation)
}

class RecommendationsListAdapter(
    var recommendationsList: List<Recommandation>,
    var onItemClick: OnItemClickListener
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
        with(holder) {
            with(recommendationsList[position]) {
                Glide.with(holder.itemView.context)
                    .load(this.imageURL)
                    .apply(RequestOptions().override(77.px, 46.px))
                    .placeholder(R.drawable.new_group_illu)
                    .transform(RoundedCorners(5.px))
                    .into(binding.image)
                binding.title.text = this.name
                binding.root.setOnClickListener {
                    onItemClick.onItemClick(this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return recommendationsList.size.coerceAtMost(Const.LIMIT_RECOMMENDATION)
    }
}
