package social.entourage.android.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.LayoutHomeToolsBinding

class HomeToolsAdapter(
    private val context: Context,
    private val onMapClick: () -> Unit,
    private val onPedagoClick: () -> Unit,
    private val onCharterClick: () -> Unit,
    private val onClimateMapClick: () -> Unit
) : RecyclerView.Adapter<HomeToolsAdapter.ToolsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolsViewHolder {
        val binding = LayoutHomeToolsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToolsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolsViewHolder, position: Int) {
        holder.binding.cardToolsMap.setOnClickListener { onMapClick() }
        holder.binding.cardToolsPedago.setOnClickListener { onPedagoClick() }
        holder.binding.cardToolsCharter.setOnClickListener { onCharterClick() }
        holder.binding.cardClimateMap.setOnClickListener { onClimateMapClick() }
    }

    override fun getItemCount(): Int = 1

    class ToolsViewHolder(val binding: LayoutHomeToolsBinding) : RecyclerView.ViewHolder(binding.root)
}
