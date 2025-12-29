package social.entourage.android.discussions.imageviewier

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.ConversationImage

class ImageGridAdapter(
    private var items: List<ConversationImage>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImageGridAdapter.Holder>() {

    interface OnImageClickListener {
        fun onImageClicked(chatMessageId: Int)
    }

    fun submitList(newItems: List<ConversationImage>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_image_thumb, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        Glide.with(holder.image.context)
            .load(item.url.orEmpty())
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val id = item.chatMessageId ?: -1
            if (id != -1) listener.onImageClicked(id)
        }
    }

    override fun getItemCount(): Int = items.size

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image_thumb)
    }
}



class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        val col = pos % spanCount
        outRect.left = spacingPx - col * spacingPx / spanCount
        outRect.right = (col + 1) * spacingPx / spanCount
        if (pos < spanCount) outRect.top = spacingPx
        outRect.bottom = spacingPx
    }
}
