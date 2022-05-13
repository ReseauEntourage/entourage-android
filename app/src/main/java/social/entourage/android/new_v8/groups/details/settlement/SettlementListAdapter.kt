package social.entourage.android.new_v8.groups.details.settlement


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewSettlementItemBinding
import social.entourage.android.new_v8.models.Settlement

class SettlementListAdapter(
    var settlementList: List<Settlement>
) : RecyclerView.Adapter<SettlementListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewSettlementItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewSettlementItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(settlementList[position]) {
                binding.title.text = this.title
                binding.content.text = this.content
                binding.position.text = String.format("%02d", (position + 1)).plus(".")
            }
        }
    }

    override fun getItemCount(): Int {
        return settlementList.size
    }
}