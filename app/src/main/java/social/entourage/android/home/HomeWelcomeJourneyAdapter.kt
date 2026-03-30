package social.entourage.android.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import social.entourage.android.R
import social.entourage.android.tools.utils.Const

class HomeWelcomeJourneyAdapter(
    private val context: Context,
    private val onStepClick: (stepIndex: Int) -> Unit
) : RecyclerView.Adapter<HomeWelcomeJourneyAdapter.ViewHolder>() {

    private var step1Completed = false
    private var step2Completed = false
    private var step3Completed = false
    private var isVisible = true
    private var isCompletedFully = false

    fun updateStepState(stepIndex: Int, isCompleted: Boolean) {
        when (stepIndex) {
            1 -> step1Completed = isCompleted
            2 -> step2Completed = isCompleted
            3 -> step3Completed = isCompleted
        }
        // Utilisation d'un payload pour éviter l'animation de clignotement / recréation de vue
        notifyItemChanged(0, "REFRESH_STATE")
    }

    fun setVisible(visible: Boolean) {
        isVisible = visible
        notifyDataSetChanged()
    }

    fun setFullyCompleted(completed: Boolean) {
        isCompletedFully = completed
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_welcome_journey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    // Surcharge avec payload pour mettre à jour l'UI sans détruire/recréer la vue
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.bind()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvStepCounter: TextView = view.findViewById(R.id.tv_step_counter)
        private val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        private val tvMicrocopy: TextView = view.findViewById(R.id.tv_microcopy)

        // Step 1
        private val cardStep1: ConstraintLayout = view.findViewById(R.id.card_step_1)
        private val tvTitleStep1: TextView = view.findViewById(R.id.tv_title_step_1)
        private val tvDescStep1: TextView = view.findViewById(R.id.tv_desc_step_1)
        private val tvBadgeStep1: TextView = view.findViewById(R.id.tv_badge_step_1)
        private val btnStep1: MaterialButton = view.findViewById(R.id.btn_step_1)
        private val ivIconStep1: android.widget.ImageView = view.findViewById(R.id.iv_icon_step_1)

        // Step 2
        private val cardStep2: ConstraintLayout = view.findViewById(R.id.card_step_2)
        private val tvTitleStep2: TextView = view.findViewById(R.id.tv_title_step_2)
        private val tvDescStep2: TextView = view.findViewById(R.id.tv_desc_step_2)
        private val tvBadgeStep2: TextView = view.findViewById(R.id.tv_badge_step_2)
        private val btnStep2: MaterialButton = view.findViewById(R.id.btn_step_2)
        private val ivIconStep2: android.widget.ImageView = view.findViewById(R.id.iv_icon_step_2)

        // Step 3
        private val cardStep3: ConstraintLayout = view.findViewById(R.id.card_step_3)
        private val tvTitleStep3: TextView = view.findViewById(R.id.tv_title_step_3)
        private val tvDescStep3: TextView = view.findViewById(R.id.tv_desc_step_3)
        private val tvBadgeStep3: TextView = view.findViewById(R.id.tv_badge_step_3)
        private val btnStep3: MaterialButton = view.findViewById(R.id.btn_step_3)
        private val ivIconStep3: android.widget.ImageView = view.findViewById(R.id.iv_icon_step_3)

        // Success state
        private val layoutSuccess: ConstraintLayout = view.findViewById(R.id.layout_success)

        fun bind() {
            var completedCount = 0
            if (step1Completed) completedCount++
            if (step2Completed) completedCount++
            if (step3Completed) completedCount++

            tvStepCounter.text = "$completedCount/3"
            progressBar.progress = completedCount

            // Microcopy
            when (completedCount) {
                0 -> tvMicrocopy.text = "Commençons 💛"
                1 -> tvMicrocopy.text = "Vous êtes bien lancé(e) ✨"
                2 -> tvMicrocopy.text = "Plus qu'une étape pour rejoindre la communauté 🎉"
                else -> tvMicrocopy.text = "Parcours terminé !"
            }

            if (isCompletedFully) {
                // Success message visible, steps hidden
                layoutSuccess.visibility = View.VISIBLE
                cardStep1.visibility = View.GONE
                cardStep2.visibility = View.GONE
                cardStep3.visibility = View.GONE
                return
            } else {
                layoutSuccess.visibility = View.GONE
                cardStep1.visibility = View.VISIBLE
                cardStep2.visibility = View.VISIBLE
                cardStep3.visibility = View.VISIBLE
            }

            // --- STEP 1 LOGIC ---
            if (step1Completed) {
                cardStep1.alpha = 0.5f // disable effect
                cardStep1.setBackgroundResource(R.drawable.bg_welcome_step_completed)
                tvTitleStep1.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvDescStep1.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep1.text = "Terminé"
                tvBadgeStep1.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep1.setBackgroundResource(R.drawable.bg_badge_completed)
                btnStep1.visibility = View.GONE
                cardStep1.setOnClickListener(null)
                cardStep1.isClickable = false
                cardStep1.isEnabled = false
                ivIconStep1.setImageResource(R.drawable.ic_lucide_check)
                ivIconStep1.setBackgroundResource(R.drawable.bg_circle_green_lucide)
            } else {
                cardStep1.alpha = 1.0f
                cardStep1.setBackgroundResource(R.drawable.bg_welcome_step_active)
                tvTitleStep1.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvDescStep1.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvBadgeStep1.text = "À faire"
                tvBadgeStep1.setTextColor(ContextCompat.getColor(context, R.color.orange))
                tvBadgeStep1.setBackgroundResource(R.drawable.bg_badge_todo)
                btnStep1.visibility = View.VISIBLE
                btnStep1.isEnabled = true
                btnStep1.setOnClickListener { onStepClick(1) }
                cardStep1.setOnClickListener { onStepClick(1) }
                cardStep1.isClickable = true
                cardStep1.isEnabled = true
                ivIconStep1.setImageResource(R.drawable.ic_lucide_video)
                ivIconStep1.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
            }

            // --- STEP 2 LOGIC ---
            if (step2Completed) {
                cardStep2.alpha = 0.5f // disable effect
                cardStep2.setBackgroundResource(R.drawable.bg_welcome_step_completed)
                tvTitleStep2.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvDescStep2.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep2.visibility = View.VISIBLE
                tvBadgeStep2.text = "Terminé"
                tvBadgeStep2.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep2.setBackgroundResource(R.drawable.bg_badge_completed)
                btnStep2.visibility = View.GONE
                cardStep2.setOnClickListener(null)
                cardStep2.isClickable = false
                cardStep2.isEnabled = false
                ivIconStep2.setImageResource(R.drawable.ic_lucide_check)
                ivIconStep2.setBackgroundResource(R.drawable.bg_circle_green_lucide)
            } else if (step1Completed) { // Step 2 is active
                cardStep2.alpha = 1.0f // Devient opaque car débloqué
                cardStep2.setBackgroundResource(R.drawable.bg_welcome_step_active)
                tvTitleStep2.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvDescStep2.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvBadgeStep2.visibility = View.VISIBLE
                tvBadgeStep2.text = "À faire"
                tvBadgeStep2.setTextColor(ContextCompat.getColor(context, R.color.orange))
                tvBadgeStep2.setBackgroundResource(R.drawable.bg_badge_todo)
                btnStep2.visibility = View.VISIBLE
                btnStep2.isEnabled = true
                btnStep2.setOnClickListener { onStepClick(2) }
                cardStep2.setOnClickListener { onStepClick(2) }
                cardStep2.isClickable = true
                cardStep2.isEnabled = true
                ivIconStep2.setImageResource(R.drawable.ic_lucide_users)
                ivIconStep2.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
            } else { // Step 2 is future (Bloqué)
                cardStep2.alpha = 0.5f // LÉGÈREMENT TRANSPARENT
                cardStep2.setBackgroundResource(R.drawable.bg_welcome_step_future)
                tvTitleStep2.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvDescStep2.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvBadgeStep2.visibility = View.GONE
                btnStep2.visibility = View.GONE
                btnStep2.isEnabled = false
                cardStep2.setOnClickListener(null)
                cardStep2.isClickable = false // NON CLIQUABLE
                cardStep2.isEnabled = false
                ivIconStep2.setImageResource(R.drawable.ic_lucide_users)
                ivIconStep2.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
            }

            // --- STEP 3 LOGIC ---
            if (step3Completed) {
                cardStep3.alpha = 0.5f // disable effect
                cardStep3.setBackgroundResource(R.drawable.bg_welcome_step_completed)
                tvTitleStep3.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvDescStep3.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep3.visibility = View.VISIBLE
                tvBadgeStep3.text = "Terminé"
                tvBadgeStep3.setTextColor(ContextCompat.getColor(context, R.color.green))
                tvBadgeStep3.setBackgroundResource(R.drawable.bg_badge_completed)
                btnStep3.visibility = View.GONE
                cardStep3.setOnClickListener(null)
                cardStep3.isClickable = false
                cardStep3.isEnabled = false
                ivIconStep3.setImageResource(R.drawable.ic_lucide_check)
                ivIconStep3.setBackgroundResource(R.drawable.bg_circle_green_lucide)
            } else if (step2Completed) { // Step 3 is active
                cardStep3.alpha = 1.0f // Devient opaque car débloqué
                cardStep3.setBackgroundResource(R.drawable.bg_welcome_step_active)
                tvTitleStep3.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvDescStep3.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvBadgeStep3.visibility = View.VISIBLE
                tvBadgeStep3.text = "À faire"
                tvBadgeStep3.setTextColor(ContextCompat.getColor(context, R.color.orange))
                tvBadgeStep3.setBackgroundResource(R.drawable.bg_badge_todo)
                btnStep3.visibility = View.VISIBLE
                btnStep3.isEnabled = true
                btnStep3.setOnClickListener { onStepClick(3) }
                cardStep3.setOnClickListener { onStepClick(3) }
                cardStep3.isClickable = true
                cardStep3.isEnabled = true
                ivIconStep3.setImageResource(R.drawable.ic_lucide_messages)
                ivIconStep3.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
            } else { // Step 3 is future (Bloqué)
                cardStep3.alpha = 0.5f // LÉGÈREMENT TRANSPARENT
                cardStep3.setBackgroundResource(R.drawable.bg_welcome_step_future)
                tvTitleStep3.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvDescStep3.setTextColor(ContextCompat.getColor(context, R.color.grey))
                tvBadgeStep3.visibility = View.GONE
                btnStep3.visibility = View.GONE
                btnStep3.isEnabled = false
                cardStep3.setOnClickListener(null)
                cardStep3.isClickable = false // NON CLIQUABLE
                cardStep3.isEnabled = false
                ivIconStep3.setImageResource(R.drawable.ic_lucide_messages)
                ivIconStep3.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
            }
        }
    }
}