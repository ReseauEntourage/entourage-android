package social.entourage.android.user.role;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

/**
 * UI information of an user role
 * Created by Mihai Ionescu on 18/05/2018.
 */
public class UserRole {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final String name;
    private final @StringRes int nameResourceId;
    private final @ColorRes int colorResourceId;
    private final boolean visible;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    UserRole(String name, @StringRes int nameResourceId, @ColorRes int colorResourceId, boolean visible) {
        this.name = name;
        this.nameResourceId = nameResourceId;
        this.colorResourceId = colorResourceId;
        this.visible = visible;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getName() {
        return name;
    }

    public int getNameResourceId() {
        return nameResourceId;
    }

    public int getColorResourceId() {
        return colorResourceId;
    }

    public boolean isVisible() {
        return visible;
    }

}
