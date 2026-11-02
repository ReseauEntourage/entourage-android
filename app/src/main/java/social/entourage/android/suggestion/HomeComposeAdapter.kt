package social.entourage.android.suggestion

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter à un seul item Compose pour la ConcatAdapter de la home. L'état est un snapshot state :
 * un changement de contenu se recompose sans notifier le RecyclerView ; seul le passage
 * masqué / visible insère ou retire l'item.
 */
class HomeComposeAdapter<S>(
    initial: S,
    private val isVisible: (S) -> Boolean,
    private val content: @Composable (S) -> Unit,
) : RecyclerView.Adapter<HomeComposeAdapter.ViewHolder>() {

    private var state by mutableStateOf(initial)
    private var shown = isVisible(initial)

    fun submit(newState: S) {
        state = newState
        val nowVisible = isVisible(newState)
        if (nowVisible != shown) {
            shown = nowVisible
            if (nowVisible) notifyItemInserted(0) else notifyItemRemoved(0)
        }
    }

    override fun getItemCount(): Int = if (shown) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { content(state) }
        }
        return ViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit

    class ViewHolder(view: ComposeView) : RecyclerView.ViewHolder(view)
}
