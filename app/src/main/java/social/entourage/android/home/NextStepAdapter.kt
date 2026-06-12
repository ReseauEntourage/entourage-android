package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.NextStep
import social.entourage.android.databinding.FragmentNextStepCardBinding

class NextStepAdapter(
    private val onCtaClick: (NextStep) -> Unit,
    private val onDismissClick: (NextStep) -> Unit
) : RecyclerView.Adapter<NextStepAdapter.NextStepViewHolder>() {

    private var nextStep: NextStep? = null

    fun update(step: NextStep?) {
        val wasVisible = nextStep != null
        val isVisible = step != null
        nextStep = step
        if (wasVisible != isVisible || (wasVisible && isVisible)) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = if (nextStep != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextStepViewHolder {
        val binding = FragmentNextStepCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NextStepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NextStepViewHolder, position: Int) {
        val step = nextStep ?: return
        holder.bind(step, onCtaClick, onDismissClick)
    }

    class NextStepViewHolder(
        private val binding: FragmentNextStepCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            step: NextStep,
            onCtaClick: (NextStep) -> Unit,
            onDismissClick: (NextStep) -> Unit
        ) {
            binding.nextStepCardRoot.visibility = View.VISIBLE
            binding.tvNextStepTitle.text = step.title
            binding.btnNextStepCta.text = step.ctaLabel

            if (!step.reason.isNullOrBlank()) {
                binding.tvNextStepReason.text = step.reason
                binding.tvNextStepReason.visibility = View.VISIBLE
            } else {
                binding.tvNextStepReason.visibility = View.GONE
            }

            binding.btnNextStepCta.setOnClickListener { onCtaClick(step) }
            binding.btnNextStepDismiss.setOnClickListener { onDismissClick(step) }
        }
    }
}
