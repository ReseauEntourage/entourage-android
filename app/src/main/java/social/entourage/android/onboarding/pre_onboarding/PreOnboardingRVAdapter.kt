package social.entourage.android.onboarding.pre_onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutCellPreOnboardingBinding

class PreOnboardingRVAdapter(val context: Context, private val myDataset: ArrayList<Int>) :
    RecyclerView.Adapter<PreOnboardingRVAdapter.ImageVH>() {

    inner class ImageVH(private val binding: LayoutCellPreOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(res: Int) {
            binding.uiIvCellPreonboard.setImageResource(res)

            // Vérifie si l'interface est en RTL
            val isRtl = itemView.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
            if (isRtl) {
                binding.uiIvCellPreonboard.scaleX = -1f // Applique un effet miroir horizontal en RTL
            } else {
                binding.uiIvCellPreonboard.scaleX = 1f // Réinitialise la transformation si non-RTL
            }
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
