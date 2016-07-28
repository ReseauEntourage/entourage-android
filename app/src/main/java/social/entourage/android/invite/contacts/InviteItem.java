package social.entourage.android.invite.contacts;

/**
 * Created by mihaiionescu on 28/07/16.
 */
public class InviteItem {

    protected static final int TYPE_ITEM = 0;
    protected static final int TYPE_SEPARATOR = 1;

    private int itemType = TYPE_ITEM;
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
