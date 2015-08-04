package social.entourage.android.message;

import javax.inject.Inject;

/**
 * Presenter controlling the MessageActivity
 * @see MessageActivity
 */
public class MessagePresenter {

    private final MessageActivity activity;

    @Inject
    public MessagePresenter(final MessageActivity activity) {
        this.activity = activity;
    }

}
