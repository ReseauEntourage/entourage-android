package social.entourage.android.tour.choice;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to ChoiceFragment lifecycle
 * Provides a ChoicePresenter
 * @see ChoiceFragment
 * @see ChoicePresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ChoiceModule.class
)
@SuppressWarnings("unused")
public interface ChoiceComponent {
    void inject(ChoiceFragment fragment);

    ChoicePresenter getChoicePresenter();
}
