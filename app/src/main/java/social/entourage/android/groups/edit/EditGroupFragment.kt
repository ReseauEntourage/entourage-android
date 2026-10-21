package social.entourage.android.groups.edit

import social.entourage.android.groups.create.CreateGroupFragment
import social.entourage.android.tools.utils.Const

/**
 * Modification d'un groupe (réservée à l'admin, voir l'écran du groupe) : réutilise l'assistant de
 * création ([CreateGroupFragment]), pré-rempli avec les valeurs du groupe et terminé par l'aperçu
 * « Modifier ».
 */
class EditGroupFragment : CreateGroupFragment() {

    override val editedGroupId: Int?
        get() = arguments?.getInt(Const.GROUP_ID, Const.DEFAULT_VALUE)?.takeIf { it != Const.DEFAULT_VALUE }
}
