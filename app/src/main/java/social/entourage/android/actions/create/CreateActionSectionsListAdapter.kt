package social.entourage.android.actions.create

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_action_create_cat_infos.view.*
import kotlinx.android.synthetic.main.new_action_create_cat_item.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils

interface OnItemCheckListener {
    fun onItemCheck(position: Int)
}

class CreateActionSectionsListAdapter(
    private var actionsList: List<ActionSection>,
    private val isDemand:Boolean,
    private val onItemClick: OnItemCheckListener,
) : RecyclerView.Adapter<CreateActionSectionsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(category: ActionSection, position:Int) {
            if (category.isSelected) {
                binding.title.typeface = Typeface.create(binding.title.typeface,Typeface.BOLD)
            }
            else {
                binding.title.typeface = Typeface.create(binding.title.typeface,Typeface.NORMAL)
            }
            Log.wtf("wtf", "bind: ${category.title}")
            binding.title.text = EventUtils.showTagTranslated( binding.context,category.title!!)
            binding.subtitle.text = EventUtils.showSubTagTranslated( binding.context,category.title!!)
            binding.checkBox.isChecked = category.isSelected
            binding.icon.setImageResource(category.icon)

            binding.layout.setOnClickListener {
                onItemClick.onItemCheck(position)
            }
        }

        fun bindInfos() {
            val _ctx = binding.layout_infos.context
            binding.ui_tv_message_info.text = _ctx.getString(R.string.action_crete_cat_about,if (isDemand) _ctx.getString(R.string.action_name_demand) else _ctx.getString(R.string.action_name_contrib))
            binding.layout_infos.setOnClickListener {
                onItemClick.onItemCheck(-1)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.new_action_create_cat_infos, parent, false)

            return ViewHolder(view)
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_action_create_cat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.bindInfos()
            return
        }

        holder.bind(actionsList[position], position)
    }

    override fun getItemCount(): Int {
        return actionsList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == actionsList.size) return 1

        return 0
    }
}