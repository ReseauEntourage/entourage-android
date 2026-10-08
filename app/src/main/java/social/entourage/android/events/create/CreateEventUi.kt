package social.entourage.android.events.create

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes

/** Affiche (ou masque) le message d'erreur placé sous un champ. */
internal fun TextView.bindError(@StringRes message: Int?) {
    if (message == null) {
        visibility = View.GONE
        text = ""
    } else {
        setText(message)
        visibility = View.VISIBLE
    }
}

/** Passe un champ en état d'erreur (bordure rouge) : c'est l'état `activated` de son fond. */
internal fun View.bindErrorState(hasError: Boolean) {
    isActivated = hasError
}
