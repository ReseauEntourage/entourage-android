package social.entourage.android.groups.choosePhoto

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewPhotoItemBinding
import social.entourage.android.databinding.NewPhotoAddItemBinding
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px

class ChoosePhotoAdapter(
    var photosList: List<Image>,
    var isEvent: Boolean,
    var showAddPhotoItem: Boolean = false,
    var onAddPhotoClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var checkedPosition = -1

    companion object {
        const val TYPE_ADD = 0
        const val TYPE_PHOTO = 1
    }

    override fun getItemViewType(position: Int): Int {
        if (showAddPhotoItem && position == 0) {
            return TYPE_ADD
        }
        return TYPE_PHOTO
    }

    inner class PhotoViewHolder(val binding: NewPhotoItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AddPhotoViewHolder(val binding: NewPhotoAddItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val binding = NewPhotoAddItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AddPhotoViewHolder(binding)
        } else {
            val binding = NewPhotoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PhotoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ADD) {
            (holder as AddPhotoViewHolder).binding.layoutAddPhoto.setOnClickListener {
                onAddPhotoClick?.invoke()
            }
        } else {
            val photoPosition = if (showAddPhotoItem) position - 1 else position
            val photoHolder = holder as PhotoViewHolder
            with(photoHolder) {
                with(photosList[photoPosition]) {
                    var imageUrl: String?

                    if (isEvent) {
                        imageUrl = if (this.portraitUrl != null) this.portraitUrl else this.landscapeUrl
                    } else {
                        imageUrl = this.imageUrl
                    }

                    imageUrl?.let {
                        Glide.with(binding.image.context)
                            .load(Uri.parse(it))
                            .apply(RequestOptions().override(90.px, 90.px))
                            .transform(
                                CenterCrop(),
                                RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px)
                            )
                            .placeholder(R.drawable.placeholder_user)
                            .into(binding.image)
                    }
                    if (photosList[photoPosition].isSelected == true) {
                        checkedPosition = photoPosition
                        binding.image.setBackgroundResource(R.drawable.new_bg_choose_photo_selected)
                    } else {
                        binding.image.setBackgroundResource(0)
                    }

                    binding.image.setOnClickListener {
                        if (checkedPosition != -1) {
                            photosList[checkedPosition].isSelected = false
                            notifyItemChanged(if (showAddPhotoItem) checkedPosition + 1 else checkedPosition)
                        }
                        photosList[photoPosition].isSelected = true
                        checkedPosition = photoPosition
                        notifyItemChanged(position)
                    }
                }
            }
        }
    }

    fun getSelected(): Image? {
        return if (checkedPosition != -1) {
            photosList[checkedPosition]
        } else null
    }

    override fun getItemCount(): Int {
        return if (showAddPhotoItem) photosList.size + 1 else photosList.size
    }
}
