package social.entourage.android.api.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 25/05/2018.
 */
public class UserMembership implements Serializable {

    private static final long serialVersionUID = -1826012197666373939L;

    @SerializedName("id")
    private int membershipId;

    @SerializedName("title")
    private String membershipTitle;

    @SerializedName("number_of_people")
    private int numberOfPeople;

    private String type;

    public int getMembershipId() {
        return membershipId;
    }

    public @NotNull String getMembershipUUID() {
        //TODO Return an UUID from the server
        return String.valueOf(membershipId);
    }

    public void setMembershipId(final int membershipId) {
        this.membershipId = membershipId;
    }

    public String getMembershipTitle() {
        return membershipTitle;
    }

    public void setMembershipTitle(final String membershipTitle) {
        this.membershipTitle = membershipTitle;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(final int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Drawable getIconDrawable(final Context context) {
        if (BaseEntourage.GROUPTYPE_PRIVATE_CIRCLE.equalsIgnoreCase(type)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_favorite_border_black_24dp);
        }
        if (BaseEntourage.GROUPTYPE_NEIGHBORHOOD.equalsIgnoreCase(type)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_neighborhood);
        }
        return null;
    }

    public static class MembershipList implements Serializable {

        private static final long serialVersionUID = 8567771380837512524L;

        private String type;
        private ArrayList<UserMembership> list;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public ArrayList<UserMembership> getList() {
            return list;
        }

        public void setList(final ArrayList<UserMembership> list) {
            this.list = list;
        }
    }
}