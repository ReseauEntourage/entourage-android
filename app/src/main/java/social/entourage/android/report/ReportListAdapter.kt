package social.entourage.android.report

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.TagMetaData
import social.entourage.android.databinding.NewReportUserItemBinding

interface OnItemCheckListener {
    fun onItemCheck(item: TagMetaData)
    fun onItemUncheck(item: TagMetaData)
}

class ReportListAdapter(
    var reportSignalList: List<TagMetaData>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<ReportListAdapter.ViewHolder>() {

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
                binding.layout.setOnClickListener {
                    binding.checkBox.isChecked = !binding.checkBox.isChecked
                    if (binding.checkBox.isChecked) {
                        onItemClick.onItemCheck(this)
                        binding.title.setTypeface(
                            binding.title.typeface,
                            Typeface.BOLD
                        )
                    } else {
                        onItemClick.onItemUncheck(this)
                        binding.title.typeface =
                            Typeface.create(binding.title.typeface, Typeface.NORMAL)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return reportSignalList.size
    }
}