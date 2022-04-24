package social.entourage.android.new_v8.group

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.api.model.GroupImage
import social.entourage.android.api.model.MetaData
import social.entourage.android.databinding.NewChoosePhotoItemBinding

interface OnItemCheckListener {
    fun onItemCheck(item: MetaData)
    fun onItemUncheck(item: MetaData)
}

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
                    .apply(RequestOptions().override(200, 200))
                    .transform(CenterCrop(), RoundedCorners(14))
                    .into(binding.image)
                binding.image.setOnClickListener {
                    if (checkedPosition != position) {
                        notifyItemChanged(checkedPosition)
                        checkedPosition = position
                    }
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