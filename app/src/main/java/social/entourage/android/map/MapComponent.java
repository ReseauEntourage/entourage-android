package social.entourage.android.map;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to MapActivity lifecycle
 * Provide a MapPresenter
 * @see MapActivity
 * @see MapPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MapModule.class
)
@SuppressWarnings("unused")
public interface MapComponent {
    void inject(MapActivity activity);

    MapPresenter getMapPresenter();
}
