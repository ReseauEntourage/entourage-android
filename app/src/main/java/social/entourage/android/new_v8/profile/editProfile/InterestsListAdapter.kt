package social.entourage.android.new_v8.profile.editProfile

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewProfileEditInterestItemBinding
import social.entourage.android.new_v8.profile.models.Interest

interface OnItemCheckListener {
    fun onItemCheck(item: Interest)
    fun onItemUncheck(item: Interest)
}

class InterestsListAdapter(
    var interestsList: List<Interest>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<InterestsListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewProfileEditInterestItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewProfileEditInterestItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(interestsList[position]) {
                if (this.isSelected) binding.title.setTypeface(
                    binding.title.typeface,
                    Typeface.BOLD
                )
                binding.title.text = this.title
                binding.checkBox.isChecked = this.isSelected == true
                this.icon.let { binding.icon.setImageResource(it) }
                binding.layout.setOnClickListener {
                    if (this.isSelected) {
                        onItemClick.onItemUncheck(this)
                        binding.title.typeface =
                            Typeface.create(binding.title.typeface, Typeface.NORMAL)
                    } else {
                        onItemClick.onItemCheck(this)
                        binding.title.setTypeface(
                            binding.title.typeface,
                            Typeface.BOLD
                        )
                    }
                    this.isSelected = !(this.isSelected)
                    binding.checkBox.isChecked = !binding.checkBox.isChecked
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return interestsList.size
    }
}