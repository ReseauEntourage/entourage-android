package social.entourage.android.new_v8.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.MetaData
import social.entourage.android.databinding.NewReportUserItemBinding

interface OnItemCheckListener {
    fun onItemCheck(item: MetaData)
    fun onItemUncheck(item: MetaData)
}

class ReportUserListAdapter(
    var reportSignalList: List<MetaData>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<ReportUserListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewReportUserItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewReportUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(reportSignalList[position]) {
                binding.title.text = this.name
                binding.checkBox.setOnClickListener {
                    if (!binding.checkBox.isChecked) onItemClick.onItemUncheck(this)
                    else onItemClick.onItemCheck(this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return reportSignalList.size
    }
}