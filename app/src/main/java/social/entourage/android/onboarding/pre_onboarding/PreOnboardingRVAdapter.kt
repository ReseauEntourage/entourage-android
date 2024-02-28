package social.entourage.android.onboarding.pre_onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutCellPreOnboardingBinding // Importe le bon nom de fichier de binding généré.

class PreOnboardingRVAdapter(val context: Context, private val myDataset: ArrayList<Int>) :
    RecyclerView.Adapter<PreOnboardingRVAdapter.ImageVH>() {

    inner class ImageVH(private val binding: LayoutCellPreOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(res: Int) {
            binding.uiIvCellPreonboard.setImageResource(res) // Assure-toi d'avoir le bon ID après avoir enlevé les synthetics.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding = LayoutCellPreOnboardingBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImageVH(binding)
    }

    override fun getItemCount(): Int = myDataset.size

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        holder.bind(myDataset[position])
    }
}
