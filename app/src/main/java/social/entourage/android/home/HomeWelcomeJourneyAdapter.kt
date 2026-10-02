package social.entourage.android.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import social.entourage.android.R

class HomeWelcomeJourneyAdapter(
    private val context: Context,
    private val onStepClick: (stepIndex: Int) -> Unit,
    private val onSkipClick: (stepIndex: Int) -> Unit,
    private val onMoreClick: (anchor: View) -> Unit
) : RecyclerView.Adapter<HomeWelcomeJourneyAdapter.ViewHolder>() {

    private var states: List<WelcomeJourneyState> = List(WelcomeJourneyStep.entries.size) { WelcomeJourneyState.TODO }
    private var isVisible = true

    // Met à jour toutes les étapes d'un coup (depuis le summary)
    fun updateStates(newStates: List<WelcomeJourneyState>) {
        states = newStates
        notifyItemChanged(0, "REFRESH_STATE")
    }

    fun setVisible(visible: Boolean) {
        if (isVisible != visible) {
            isVisible = visible
            notifyDataSetChanged()
        }
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

    private class StepViews(
        val card: ConstraintLayout,
        val icon: ImageView,
        val title: TextView,
        val desc: TextView,
        val badge: TextView,
        val button: MaterialButton,
        val skip: TextView,
        val redo: TextView
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvStepCounter: TextView = view.findViewById(R.id.tv_step_counter)
        private val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        private val tvMicrocopy: TextView = view.findViewById(R.id.tv_microcopy)
        private val layoutSuccess: ConstraintLayout = view.findViewById(R.id.layout_success)

        private val steps: List<StepViews> = listOf(
            stepViews(view, 1), stepViews(view, 2), stepViews(view, 3), stepViews(view, 4)
        )

        private val todoIcons = listOf(
            R.drawable.ic_lucide_video,
            R.drawable.ic_group_welcome_home,
            R.drawable.ic_lucide_users,
            R.drawable.ic_lucide_messages
        )
        private val doneIcons = listOf(
            R.drawable.ic_lucide_check,
            R.drawable.ic_group_welcome_home,
            R.drawable.ic_lucide_check,
            R.drawable.ic_lucide_check
        )
        private val todoButtonLabels = listOf(
            context.getString(R.string.welcome_video_cta),
            "Voir les groupes",
            "Voir les prochaines dates",
            "Voir les prochaines sessions"
        )

        init {
            view.findViewById<View>(R.id.btn_more).setOnClickListener { onMoreClick(it) }
        }

        private fun stepViews(root: View, n: Int): StepViews {
            fun id(name: String) = context.resources.getIdentifier("${name}_$n", "id", context.packageName)
            return StepViews(
                card = root.findViewById(id("card_step")),
                icon = root.findViewById(id("iv_icon_step")),
                title = root.findViewById(id("tv_title_step")),
                desc = root.findViewById(id("tv_desc_step")),
                badge = root.findViewById(id("tv_badge_step")),
                button = root.findViewById(id("btn_step")),
                skip = root.findViewById(id("btn_skip_step")),
                redo = root.findViewById(id("btn_redo_step"))
            )
        }

        fun bind() {
            val completedCount = states.count { it == WelcomeJourneyState.DONE }
            val isCompletedFully = completedCount == steps.size

            tvStepCounter.text = "$completedCount/${steps.size}"
            progressBar.progress = completedCount

            tvMicrocopy.text = when (completedCount) {
                0 -> "À votre rythme, dans l'ordre que vous voulez."
                1 -> "C'est bien parti ! Continuez à votre rythme."
                2 -> "Vous faites partie d'une communauté 🙌"
                3 -> "Plus qu'une étape pour rejoindre la communauté 🎉"
                else -> "Parcours terminé !"
            }

            layoutSuccess.visibility = if (isCompletedFully) View.VISIBLE else View.GONE
            steps.forEach { it.card.visibility = if (isCompletedFully) View.GONE else View.VISIBLE }
            if (isCompletedFully) return

            // Plus de verrouillage : chaque étape est accessible quel que soit l'état des autres
            steps.forEachIndexed { i, views ->
                val n = i + 1
                when (states[i]) {
                    WelcomeJourneyState.DONE -> bindDone(views, i, n)
                    WelcomeJourneyState.SKIPPED -> bindSkipped(views, i, n)
                    WelcomeJourneyState.TODO -> bindTodo(views, i, n)
                }
            }
        }

        private fun color(res: Int) = ContextCompat.getColor(context, res)

        private fun bindTodo(v: StepViews, i: Int, n: Int) {
            v.card.alpha = 1.0f
            v.card.setBackgroundResource(R.drawable.bg_welcome_step_active)
            v.title.setTextColor(color(R.color.black))
            v.desc.setTextColor(color(R.color.grey))
            v.badge.visibility = View.VISIBLE
            v.badge.text = "À faire"
            v.badge.setTextColor(color(R.color.orange))
            v.badge.setBackgroundResource(R.drawable.bg_badge_todo)
            v.button.visibility = View.VISIBLE
            v.button.isEnabled = true
            v.button.text = todoButtonLabels[i]
            v.button.setBackgroundColor(color(R.color.orange))
            v.button.setTextColor(color(R.color.white))
            v.button.setOnClickListener { onStepClick(n) }
            v.skip.visibility = View.VISIBLE
            v.skip.setOnClickListener { onSkipClick(n) }
            v.redo.visibility = View.GONE
            v.card.setOnClickListener { onStepClick(n) }
            v.card.isClickable = true
            v.card.isEnabled = true
            v.icon.setImageResource(todoIcons[i])
            v.icon.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
        }

        private fun bindDone(v: StepViews, i: Int, n: Int) {
            v.card.alpha = 0.5f
            v.card.setBackgroundResource(R.drawable.bg_welcome_step_completed)
            v.title.setTextColor(color(R.color.green))
            v.desc.setTextColor(color(R.color.green))
            v.badge.visibility = View.VISIBLE
            v.badge.text = "Terminé"
            v.badge.setTextColor(color(R.color.green))
            v.badge.setBackgroundResource(R.drawable.bg_badge_completed)
            v.skip.visibility = View.GONE
            v.redo.visibility = View.GONE
            // Les étapes 1 et 4 gardent un bouton vert (revoir la vidéo / message de fin)
            when (n) {
                1 -> showDoneButton(v, "Revoir la vidéo")
                4 -> showDoneButton(v, "Vous faites partie d'une communauté 🙌")
                else -> v.button.visibility = View.GONE
            }
            v.button.setOnClickListener { onStepClick(n) }
            v.card.setOnClickListener { onStepClick(n) }
            v.card.isClickable = true
            v.card.isEnabled = true
            v.icon.setImageResource(doneIcons[i])
            v.icon.setBackgroundResource(R.drawable.bg_circle_green_lucide)
        }

        private fun showDoneButton(v: StepViews, label: String) {
            v.button.visibility = View.VISIBLE
            v.button.text = label
            v.button.setBackgroundColor(color(R.color.green))
            v.button.setTextColor(color(R.color.white))
        }

        private fun bindSkipped(v: StepViews, i: Int, n: Int) {
            v.card.alpha = 1.0f
            v.card.setBackgroundResource(R.drawable.bg_welcome_step_future)
            v.title.setTextColor(color(R.color.grey))
            v.desc.setTextColor(color(R.color.grey))
            v.badge.visibility = View.VISIBLE
            v.badge.text = context.getString(R.string.welcome_journey_badge_skipped)
            v.badge.setTextColor(color(R.color.grey))
            v.badge.setBackgroundResource(R.drawable.bg_badge_future)
            v.button.visibility = View.GONE
            v.skip.visibility = View.GONE
            v.redo.visibility = View.VISIBLE
            v.redo.setOnClickListener { onStepClick(n) }
            v.card.setOnClickListener { onStepClick(n) }
            v.card.isClickable = true
            v.card.isEnabled = true
            v.icon.setImageResource(todoIcons[i])
            v.icon.setBackgroundResource(R.drawable.bg_circle_orange_lucide)
        }
    }
}
