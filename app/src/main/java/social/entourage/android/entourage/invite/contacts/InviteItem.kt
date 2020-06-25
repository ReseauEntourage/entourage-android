package social.entourage.android.entourage.invite.contacts

/**
 * Created by mihaiionescu on 28/07/16.
 */
open class InviteItem(val itemType: Int, val itemText: String) {
    var isSelected = false

    companion object {
        const val TYPE_CONTACT_NAME = 0
        const val TYPE_CONTACT_PHONE = 1
        const val TYPE_SECTION = 2
        const val TYPE_COUNT = 3
    }

}

class InviteItemContactName(itemText: String, val cursorPosition: Int)
    : InviteItem(TYPE_CONTACT_NAME, itemText)

class InviteItemContactPhone(itemText: String, val cursorPosition: Int)
    : InviteItem(TYPE_CONTACT_PHONE, itemText)

class InviteItemSection(itemText: String)
    : InviteItem(TYPE_SECTION, itemText)