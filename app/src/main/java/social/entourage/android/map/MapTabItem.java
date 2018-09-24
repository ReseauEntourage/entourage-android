package social.entourage.android.map;

/**
 * Created by Mihai Ionescu on 20/09/2018.
 */
public enum MapTabItem {
    ALL_TAB(0),
    EVENTS_TAB(1);

    private int id;

    MapTabItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
