package social.entourage.android.entourage

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_cell_share_entourage.view.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.SharingEntourage
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.tools.CropCircleTransformation

/**
 * Created by Jr (MJ-DEVS) on 09/09/2020.
 */
class ShareEntourageAdapter(val context: Context, private val myDataset: ArrayList<SharingEntourage>, val listener:(position:Int) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val sharingEntourage = myDataset[position]

            itemView.ui_tv_title?.text = sharingEntourage.title

            var imageUrl:String? = null
            var bitDraw: Drawable?

            if (sharingEntourage.isSelected) {
                val img = AppCompatResources.getDrawable(context,R.drawable.contact_selected)
                itemView.ui_iv_select.setImageDrawable(img)
                itemView.ui_tv_title.setTypeface(null, Typeface.BOLD)
            }
            else {
                val img = AppCompatResources.getDrawable(context,R.drawable.ic_filter_rb_bg_active)
                itemView.ui_iv_select.setImageDrawable(img)
                itemView.ui_tv_title.setTypeface(null, Typeface.NORMAL)
            }

            bitDraw = getIcn(sharingEntourage.entourage_type,sharingEntourage.category)

            if (sharingEntourage.category == null && sharingEntourage.group_type == BaseEntourage.GROUPTYPE_OUTING) {
                bitDraw = AppCompatResources.getDrawable(context,R.drawable.ic_event_accent_24dp)
            }
            if (sharingEntourage.group_type == "conversation") {
                imageUrl = sharingEntourage.author?.avatarUrl
            }

            itemView.ui_iv_avatar?.let {iconView ->
                Picasso.get().cancelRequest(iconView)
                if (imageUrl != null) {
                    iconView.setImageDrawable(null)
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(CropCircleTransformation())
                            .into(iconView)
                } else {
                    iconView.setImageDrawable(bitDraw)
                }
            }

            itemView.setOnClickListener {
                listener(position)
            }
        }
    }

    fun getIcn(type:String,category:String?) : Drawable?  {
        val entourageCategory = EntourageCategoryManager.findCategory(type,category)
        AppCompatResources.getDrawable(context, entourageCategory.iconRes)?.let { categoryIcon ->
            categoryIcon.mutate()
            categoryIcon.clearColorFilter()
            categoryIcon.setColorFilter(ContextCompat.getColor(context, entourageCategory.typeColorRes), PorterDuff.Mode.SRC_IN)
            return categoryIcon
        }

        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val v = LayoutInflater.from(context)
                .inflate(R.layout.layout_cell_share_entourage,parent, false)
        return ImageVH(v)
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ImageVH
        holder.bind(position)
    }
}