package social.entourage.android.new_v8.groups.choosePhoto

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.GroupImage
import social.entourage.android.databinding.NewChoosePhotoItemBinding
import social.entourage.android.new_v8.utils.px

class ChoosePhotoAdapter(
    var photosList: List<GroupImage>,
) : RecyclerView.Adapter<ChoosePhotoAdapter.ViewHolder>() {

    private var checkedPosition = -1

    inner class ViewHolder(val binding: NewChoosePhotoItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewChoosePhotoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(photosList[position]) {
                Glide.with(binding.image.context)
                    .load(Uri.parse(this.imageUrl))
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.image)

                if (photosList[position].isSelected == true) {
                    checkedPosition = position
                    binding.image.setBackgroundResource(R.drawable.new_bg_choose_photo_selected)

                } else
                    binding.image.setBackgroundResource(0)

                binding.image.setOnClickListener {
                    if (checkedPosition != -1) {
                        photosList[checkedPosition].isSelected = false
                        notifyItemChanged(checkedPosition)
                    }
                    photosList[position].isSelected = true
                    checkedPosition = position
                    notifyItemChanged(position)
                }
            }
        }
    }

    fun getSelected(): GroupImage? {
        return if (checkedPosition != -1) {
            photosList[checkedPosition]
        } else null
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}