package social.entourage.android.tools.view

import android.app.Activity
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import social.entourage.android.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class EntourageTapPrompt(@field:IdRes @param:IdRes private val id: Int, private val primaryText: String, private val secondaryText: String?, private val next: EntourageTapPrompt?) {

    fun show(activity: Activity?) {
        MaterialTapTargetPrompt.Builder(activity!!)
                .setTarget(id)
                .setPrimaryText(primaryText)
                .setSecondaryText(secondaryText)
                .setBackgroundColour(ContextCompat.getColor(activity, R.color.accent))
                .setPromptStateChangeListener { prompt: MaterialTapTargetPrompt?, state: Int ->
                    if (next != null && state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                        next.show(activity)
                    }
                }
                .setCaptureTouchEventOutsidePrompt(true)
                .show()
    }

}