package social.entourage.android.api.model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 24/02/16.
 */
public class TourUser extends TimestampedObject implements Serializable {

    private final static String HASH_STRING_HEAD = "TourUser-";

    @SerializedName("id")
    private int userId;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @Expose(serialize = false, deserialize = true)
    private String email;

    @Expose(serialize = false, deserialize = true)
    private String status;

    @SerializedName("requested_at")
    private Date requestDate;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(final Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    @Override
    public Date getTimestamp() {
        return requestDate;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + userId;
    }

    @Override
    public int getType() {
        return TOUR_USER;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        return (this.userId == ((TourUser)o).userId) && (this.status.equals(((TourUser)o).status));
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class TourUsersWrapper {

        private List<TourUser> users;

        public List<TourUser> getUsers() {
            return users;
        }

        public void setUsers(List<TourUser> users) {
            this.users = users;
        }

    }
}
