package social.entourage.android.user.role;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

/**
 * UI information of an user role
 * Created by Mihai Ionescu on 18/05/2018.
 */
public class UserRole {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public enum Position {LEFT, RIGHT}

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final String name;
    private final @StringRes int nameResourceId;
    private final @ColorRes int colorResourceId;
    private final Position position;
    private final boolean visible;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    UserRole(String name, @StringRes int nameResourceId, @ColorRes int colorResourceId, Position position, boolean visible) {
        this.name = name;
        this.nameResourceId = nameResourceId;
        this.colorResourceId = colorResourceId;
        this.position = position;
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

    public Position getPosition() {
        return position;
    }

    public boolean isVisible() {
        return visible;
    }

}
