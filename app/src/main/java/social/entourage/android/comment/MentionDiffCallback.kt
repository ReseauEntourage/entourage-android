package social.entourage.android.comment

import androidx.recyclerview.widget.DiffUtil
import social.entourage.android.api.model.EntourageUser

/**
 * Compare deux listes de EntourageUser pour le DiffUtil.
 */
class MentionDiffCallback(
    private val oldList: List<EntourageUser>,
    private val newList: List<EntourageUser>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    /**
     * Indique si c'est le même "item" (même ID).
     * Ici, on compare userId.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        return oldUser.userId == newUser.userId
    }

    /**
     * Indique si le contenu a changé.
     * Ici, on compare par exemple le displayName.
     * On peut ajouter d'autres champs si nécessaire.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        return oldUser.displayName == newUser.displayName
    }
}
