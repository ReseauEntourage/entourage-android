package social.entourage.android.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String KEY_USER_ID = "social.entourage.android.KEY_USER_ID";
    public static final String KEY_USER = "social.entourage.android.KEY_USER";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final int id;

    private String email;

    @Expose(serialize = false, deserialize = false)
    private String phone;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    private final String token;

    @Expose(serialize = false, deserialize = true)
    private final Stats stats;

    @Expose(serialize = false, deserialize = true)
    private final Organization organization;

    @SerializedName("avatar_url")
    private String avatarURL;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    private User(final int id, final String email, final String firstName, final String lastName, final Stats stats, final Organization organization, final String token, final String avatarURL) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.stats = stats;
        this.organization = organization;
        this.token = token;
        this.avatarURL = avatarURL;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFirstName() { return firstName;}

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        StringBuilder fullname = new StringBuilder();
        if (firstName != null) fullname.append(firstName);
        if (lastName != null) {
            if (fullname.length() > 0) fullname.append(' ');
            fullname.append(lastName);
        }
        return fullname.toString();
    }

    public String getToken() {
        return token;
    }

    public Stats getStats() {
        return stats;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public void incrementTours() {
        stats.setTourCount(stats.getTourCount() + 1);
    }

    public void incrementEncouters() {
        stats.setEncounterCount(stats.getEncounterCount() + 1);
    }

    public static String decodeURL(String encodedURL) {
        if (encodedURL == null) {
            return encodedURL;
        }
        return encodedURL.replace('\u0026', '&');
    }

    // ----------------------------------
    // BUILDER
    // ----------------------------------

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private int id;
        private String email, firstName, lastName, token, avatarURL;
        private Stats stats;
        private Organization organization;

        public Builder() {
        }

        public Builder withId(final int id) {
            this.id = id;
            return this;
        }

        public Builder withEmail(final String email) {
            this.email = email;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withStats(final Stats stats) {
            this.stats = stats;
            return this;
        }

        public Builder withOrganization(final Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder withToken(final String token) {
            this.token = token;
            return this;
        }

        public Builder withAvatarURL(final String avatarURL) {
            this.avatarURL = avatarURL;
            return this;
        }

        public User build() {
            if (id == -1) {
                return null;
            }
            if (email == null) {
                return null;
            }
            if (firstName == null) {
                return null;
            }
            if (lastName == null) {
                return null;
            }
            if (stats == null) {
                return null;
            }
            if (organization == null) {
                return null;
            }
            if (token == null) {
                return null;
            }
            return new User(id, email, firstName, lastName, stats, organization, token, avatarURL);
        }
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class UserWrapper {

        private User user;

        public UserWrapper(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
