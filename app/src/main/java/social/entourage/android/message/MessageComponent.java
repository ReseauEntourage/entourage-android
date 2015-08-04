package social.entourage.android.message;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;
/**
 * Component linked to MessageActivity lifecycle
 * Provide a MessagePresenter
 * @see MessageActivity
 * @see MessagePresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MessageModule.class
)
@SuppressWarnings("unused")
public interface MessageComponent {
    void inject(MessageActivity fragment);

    MessagePresenter getMessagepPresenter();
}