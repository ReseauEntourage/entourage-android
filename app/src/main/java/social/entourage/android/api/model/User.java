package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

public class User {

    private final int id;

    private final String email;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    private final String token;

    private final Stats stats;

    private final Organization organization;

    private User(final int id, final String email, final String firstName, final String lastName, final Stats stats, final Organization organization, final String token) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.stats = stats;
        this.organization = organization;
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Stats getStats() {
        return stats;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getToken() {
        return token;
    }

    public void incrementTours() {
        stats.setTourCount(stats.getTourCount() + 1);
    }

    public void incrementEncouters() {
        stats.setEncounterCount(stats.getEncounterCount() + 1);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private int id;
        private String email, firstName, lastName, token;
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
            return new User(id, email, firstName, lastName, stats, organization, token);
        }
    }
}
