package social.entourage.android.invite.contacts;

/**
 * Created by mihaiionescu on 28/07/16.
 */
public class InviteItemContact extends InviteItem {

    private int cursorPosition;

    public InviteItemContact(String itemText, int cursorPosition) {
        super(InviteItem.TYPE_ITEM, itemText);
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

}
