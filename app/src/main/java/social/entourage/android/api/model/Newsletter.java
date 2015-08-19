package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

public class Newsletter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String email;

    private boolean active;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Newsletter(String email, boolean active) {
        this.email = email;
        this.active = active;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class NewsletterWrapper {

        @SerializedName("newsletter_subscription")
        private Newsletter newsletter;

        public NewsletterWrapper(Newsletter newsletter) {
            this.newsletter = newsletter;
        }

        public Newsletter getNewsletter() {
            return newsletter;
        }

        public void setNewsletter(Newsletter newsletter) {
            this.newsletter = newsletter;
        }
    }
}
