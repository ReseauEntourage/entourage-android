package social.entourage.android.profile.editProfile

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.Interest
import social.entourage.android.databinding.LayoutProfileEditInterestItemBinding
import social.entourage.android.databinding.LayoutProfileEditInterestOpenItemBinding

interface OnItemCheckListener {
    fun onItemCheck(item: Interest)
    fun onItemUncheck(item: Interest)
}

enum class InterestsTypes(val label: String, val code: Int) {
    TYPE_OTHER("other", 0),
    TYPE_INTEREST("interest", 1),
}

class InterestsListAdapter(
    var interestsList: List<Interest>,
    var onItemClick: OnItemCheckListener,
    var isOtherInterestEnabled: Boolean
) : RecyclerView.Adapter<InterestsListAdapter.ViewHolder>() {

    private var otherInterest: String? = null

    fun getOtherInterestCategory(): String? {
        return otherInterest
    }

    inner class ViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindInterest(interest: Interest) {
            val bindingView = binding as LayoutProfileEditInterestItemBinding
            if (interest.isSelected) bindingView.title.setTypeface(
                bindingView.title.typeface,
                android.graphics.Typeface.BOLD
            )
            val context = bindingView.root.context
            bindingView.title.text = EventUtils.showTagTranslated(context ,interest.id!!)
            bindingView.checkBox.isChecked = interest.isSelected
            bindingView.icon.setImageResource(interest.icon)
            bindingView.layout.setOnClickListener {
                if (interest.isSelected) {
                    onItemClick.onItemUncheck(interest)
                    bindingView.title.typeface =
                        android.graphics.Typeface.create(
                            bindingView.title.typeface,
                            android.graphics.Typeface.NORMAL
                        )
                } else {
                    onItemClick.onItemCheck(interest)
                    bindingView.title.setTypeface(
                        bindingView.title.typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
                interest.isSelected = !(interest.isSelected)
                bindingView.checkBox.isChecked = !bindingView.checkBox.isChecked
            }
        }
        fun bindEditInterest(interest: Interest) {
            val bindingView = binding as LayoutProfileEditInterestOpenItemBinding
            if (interest.isSelected) bindingView.title.setTypeface(
                bindingView.title.typeface,
                android.graphics.Typeface.BOLD
            )
            val context = bindingView.root.context
            bindingView.title.text = EventUtils.showTagTranslated(context ,interest.id!!)
            bindingView.checkBox.isChecked = interest.isSelected
            bindingView.icon.setImageResource(interest.icon)
            bindingView.layout.setOnClickListener {
                if (interest.isSelected) {
                    onItemClick.onItemUncheck(interest)
                    bindingView.title.typeface =
                        android.graphics.Typeface.create(
                            bindingView.title.typeface,
                            android.graphics.Typeface.NORMAL
                        )
                    if (interest.id == InterestsTypes.TYPE_OTHER.label && isOtherInterestEnabled) {
                        bindingView.categoryName.visibility = View.GONE
                        bindingView.categoryNameLabel.visibility = View.GONE
                    }
                } else {
                    onItemClick.onItemCheck(interest)
                    bindingView.title.setTypeface(
                        bindingView.title.typeface,
                        android.graphics.Typeface.BOLD
                    )
                    if (interest.id == InterestsTypes.TYPE_OTHER.label && isOtherInterestEnabled) {
                        bindingView.categoryName.visibility = View.VISIBLE
                        bindingView.categoryNameLabel.visibility = View.VISIBLE
                        bindingView.categoryName.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {

                            }

                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                otherInterest = s.toString()
                            }

                            override fun afterTextChanged(s: Editable) {
                            }
                        })
                    }
                }
                interest.isSelected = !(interest.isSelected)
                bindingView.checkBox.isChecked = !bindingView.checkBox.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            InterestsTypes.TYPE_OTHER.code -> LayoutProfileEditInterestOpenItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            else -> LayoutProfileEditInterestItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val interest = interestsList[position]
        when (interest.id) {
            //InterestsTypes.TYPE_OTHER.label -> holder.bindEditInterest(interest)
            InterestsTypes.TYPE_OTHER.label -> holder.bindInterest(interest)
            else -> holder.bindInterest(interest)
        }
    }

    override fun getItemCount(): Int {
        return interestsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (interestsList[position].id) {
            //InterestsTypes.TYPE_OTHER.label -> InterestsTypes.TYPE_OTHER.code
            InterestsTypes.TYPE_OTHER.label -> InterestsTypes.TYPE_INTEREST.code
            else -> InterestsTypes.TYPE_INTEREST.code
        }
    }

}