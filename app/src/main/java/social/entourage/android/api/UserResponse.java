package social.entourage.android.api;

import social.entourage.android.api.model.User;

public class UserResponse {

    private final User user;

    public UserResponse(final User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
