package entourage.social.android.profile.editProfile

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import entourage.social.android.databinding.NewProfileEditInterestItemBinding
import entourage.social.android.profile.models.Interest

class InterestsListAdapter(
    var interestsList: List<Interest>,
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
                if (this.isSelected == true) binding.title.setTypeface(
                    binding.title.typeface,
                    Typeface.BOLD
                )
                binding.title.text = this.title
                binding.checkBox.isChecked = this.isSelected == true
                this.icon?.let { binding.icon.setImageResource(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return interestsList.size
    }
}