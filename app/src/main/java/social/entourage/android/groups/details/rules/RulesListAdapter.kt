package social.entourage.android.groups.details.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Rules
import social.entourage.android.databinding.NewRulesItemBinding

class RulesListAdapter(
    var rulesList: List<Rules>
) : RecyclerView.Adapter<RulesListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewRulesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewRulesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(rulesList[position]) {
                binding.title.text = this.title
                binding.content.text = this.content
                binding.position.text = ""
            }
        }
    }

    override fun getItemCount(): Int {
        return rulesList.size
    }
}