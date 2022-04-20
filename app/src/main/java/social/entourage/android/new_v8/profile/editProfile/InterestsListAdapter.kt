package social.entourage.android.new_v8.profile.editProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.*
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.checkBox
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.icon
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.layout
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.title
import kotlinx.android.synthetic.main.new_profile_edit_interests_edittext_item.view.*
import social.entourage.android.R
import social.entourage.android.new_v8.profile.models.Interest

interface OnItemCheckListener {
    fun onItemCheck(item: Interest)
    fun onItemUncheck(item: Interest)
}

class InterestsListAdapter(
    var interestsList: List<Interest>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<InterestsListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(interest: Interest) {
            if (interest.isSelected) binding.title.setTypeface(
                binding.title.typeface,
                android.graphics.Typeface.BOLD
            )
            binding.title.text = interest.title
            binding.checkBox.isChecked = interest.isSelected == true
            interest.icon.let { binding.icon.setImageResource(it) }
            binding.layout.setOnClickListener {
                if (interest.isSelected) {
                    onItemClick.onItemUncheck(interest)
                    binding.title.typeface =
                        android.graphics.Typeface.create(
                            binding.title.typeface,
                            android.graphics.Typeface.NORMAL
                        )
                    if (interest.id == "other") {
                        binding.category_name.visibility = View.GONE
                        binding.category_name_label.visibility = View.GONE
                    }
                } else {
                    onItemClick.onItemCheck(interest)
                    binding.title.setTypeface(
                        binding.title.typeface,
                        android.graphics.Typeface.BOLD
                    )
                    if (interest.id == "other") {
                        binding.category_name.visibility = View.VISIBLE
                        binding.category_name_label.visibility = View.VISIBLE
                    }
                }
                interest.isSelected = !(interest.isSelected)
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_OTHER -> R.layout.new_profile_edit_interests_edittext_item
            else -> R.layout.new_profile_edit_interest_item
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(layout, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(interestsList[position])
    }

    override fun getItemCount(): Int {
        return interestsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (interestsList[position].id) {
            "other" -> TYPE_OTHER
            else -> TYPE_INTEREST
        }
    }

    companion object {
        private const val TYPE_OTHER = 0
        private const val TYPE_INTEREST = 1
    }
}