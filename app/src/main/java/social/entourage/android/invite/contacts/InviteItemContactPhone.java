package social.entourage.android.invite.contacts;

/**
 * Created by mihaiionescu on 13/10/16.
 */

public class InviteItemContactPhone extends InviteItem {
    private int cursorPosition;

    public InviteItemContactPhone(String itemText, int cursorPosition) {
        super(InviteItem.TYPE_CONTACT_PHONE, itemText);
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
}
