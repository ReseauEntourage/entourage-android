package social.entourage.android.invite.contacts;

/**
 * Created by mihaiionescu on 28/07/16.
 */
public class InviteItem {

    protected static final int TYPE_CONTACT_NAME = 0;
    protected static final int TYPE_CONTACT_PHONE = 1;
    protected static final int TYPE_SECTION = 2;

    public static final int TYPE_COUNT = 3;

    private int itemType = TYPE_CONTACT_NAME;
    private String itemText;

    public InviteItem(int itemType, String itemText) {
        this.itemType = itemType;
        this.itemText = itemText;
    }

    public int getItemType() {
        return itemType;
    }

    public String getItemText() {
        return itemText;
    }

}
