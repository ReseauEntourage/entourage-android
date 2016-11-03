package social.entourage.android.invite.contacts;

/**
 * Created by mihaiionescu on 28/07/16.
 */
public class InviteItemContactName extends InviteItem {

    private int cursorPosition;

    public InviteItemContactName(String itemText, int cursorPosition) {
        super(InviteItem.TYPE_CONTACT_NAME, itemText);
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

}
