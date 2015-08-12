package social.entourage.android.map.confirmation;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to ConfirmationActivity lifecycle
 * Provide a ConfirmationPresenter
 * @see ConfirmationActivity
 * @see ConfirmationPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ConfirmationModule.class
)
@SuppressWarnings("unused")
public interface ConfirmationComponent {
    void inject(ConfirmationActivity activity);

    ConfirmationPresenter getConfirmationPresenter();
}
