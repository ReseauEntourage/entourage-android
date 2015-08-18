package social.entourage.android.api.wrapper;

import social.entourage.android.api.model.User;

public class UserWrapper {

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
