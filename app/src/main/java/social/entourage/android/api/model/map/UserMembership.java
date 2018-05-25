package social.entourage.android.api.model.map;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

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

    public int getMembershipId() {
        return membershipId;
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

    public static class MembershipList {

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
