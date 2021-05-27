package social.entourage.android.entourage.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_cell_photo_gallery.view.*
import social.entourage.android.R
import social.entourage.android.api.PhotoGallery

/**
 * Created by Jr on 21/05/2021.
 */
class EntouragePhotoGalleryAdapter(var photos: ArrayList<PhotoGallery>, val listener: PhotoGalleryListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedItemPosition = -1

    fun updateDatas(datas:ArrayList<PhotoGallery>, selectedPosition:Int) {
        photos = ArrayList()
        photos.addAll(datas)
        this.selectedItemPosition = selectedPosition
        notifyDataSetChanged()
    }

    fun updateSelection(selectedPosition:Int) {
        this.selectedItemPosition = selectedPosition
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_cell_photo_gallery, parent, false)
        return PhotoGalleryVH(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val isSelected = position == selectedItemPosition
        (holder as PhotoGalleryVH).bind(position, photos[position].url_image_landscape,isSelected, listener)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    inner class PhotoGalleryVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position:Int, photoUrl: String?, isSelected:Boolean, listener: PhotoGalleryListener) {
            itemView.setOnClickListener {

                if (!isSelected) {
                    selectedItemPosition = -1
                }
                else {
                    selectedItemPosition = position
                }
                selectImage(!isSelected)
                listener.onPhotoSelected(position)
            }

            Picasso.get().load(photoUrl).error(R.drawable.ic_placeholder_detail_event).placeholder(R.drawable.ic_placeholder_event).into(itemView.ui_event_iv)

            selectImage(isSelected)
        }

        fun selectImage(isSelected:Boolean) {
            if (isSelected) {
                itemView.ui_iv_select?.setImageResource(R.drawable.contact_selected)
            }
            else {
                itemView.ui_iv_select?.setImageResource(R.drawable.bg_button_circle_grey)
            }
        }
    }
}

interface PhotoGalleryListener {
    fun onPhotoSelected(position: Int)
}