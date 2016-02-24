package social.entourage.android.api.model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by mihaiionescu on 24/02/16.
 */
public class TourUser implements Serializable {

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

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class TourUserComparatorOldToNew implements Comparator<TourUser> {
        @Override
        public int compare(final TourUser lhs, final TourUser rhs) {
            if (lhs.getRequestDate() != null && rhs.getRequestDate() != null) {
                Date date1 = lhs.getRequestDate();
                Date date2 = rhs.getRequestDate();
                return date1.compareTo(date2);
            } else {
                return 0;
            }
        }
    }

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
