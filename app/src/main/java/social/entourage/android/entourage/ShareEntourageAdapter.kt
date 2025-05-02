package social.entourage.android.entourage

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.SharingEntourage
import social.entourage.android.databinding.LayoutCellShareEntourageBinding
import social.entourage.android.entourage.category.EntourageCategoryManager

class ShareEntourageAdapter(private val context: Context, private val myDataset: ArrayList<SharingEntourage>, private val listener: (position: Int) -> Unit) :
    RecyclerView.Adapter<ShareEntourageAdapter.ImageVH>() {

    inner class ImageVH(val binding: LayoutCellShareEntourageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val sharingEntourage = myDataset[position]

            binding.uiTvTitle.text = sharingEntourage.title

            if (sharingEntourage.isSelected) {
                val img = AppCompatResources.getDrawable(context, R.drawable.contact_selected)
                binding.uiIvSelect.setImageDrawable(img)
                binding.uiTvTitle.setTypeface(null, Typeface.BOLD)
            } else {
                val img = AppCompatResources.getDrawable(context, R.drawable.ic_filter_rb_bg_active)
                binding.uiIvSelect.setImageDrawable(img)
                binding.uiTvTitle.setTypeface(null, Typeface.NORMAL)
            }

            Glide.with(binding.uiIvAvatar.context).clear(binding.uiIvAvatar)
            val imageUrl: String? = if (sharingEntourage.group_type == "conversation") sharingEntourage.author?.avatarUrl else null

            if (imageUrl != null) {
                Glide.with(binding.uiIvAvatar.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_user_photo_small)
                    .circleCrop()
                    .into(binding.uiIvAvatar)
            } else {
                val bitDraw: Drawable? = if (sharingEntourage.category == null && sharingEntourage.group_type == BaseEntourage.GROUPTYPE_OUTING) {
                    AppCompatResources.getDrawable(context, R.drawable.ic_event_accent_24dp)
                } else {
                    getIcn(sharingEntourage.entourage_type, sharingEntourage.category)
                }
                binding.uiIvAvatar.setImageDrawable(bitDraw)
            }

            binding.root.setOnClickListener {
                listener(position)
            }
        }
    }

    private fun getIcn(type: String, category: String?): Drawable? {
        val entourageCategory = EntourageCategoryManager.findCategory(type, category)
        AppCompatResources.getDrawable(context, entourageCategory.iconRes)?.let { categoryIcon ->
            categoryIcon.mutate()
            categoryIcon.clearColorFilter()
            categoryIcon.setColorFilter(ContextCompat.getColor(context, entourageCategory.typeColorRes), PorterDuff.Mode.SRC_IN)
            return categoryIcon
        }

        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding = LayoutCellShareEntourageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageVH(binding)
    }

    override fun getItemCount(): Int = myDataset.size

    override fun onBindViewHolder(holder: ImageVH, position: Int) = holder.bind(position)
}
