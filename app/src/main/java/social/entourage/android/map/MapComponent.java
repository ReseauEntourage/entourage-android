package social.entourage.android.map;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to MapEntourageFragment lifecycle
 * Provide a MapPresenter
 * @see MapEntourageFragment
 * @see MapPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MapModule.class
)
@SuppressWarnings("unused")
public interface MapComponent {
    void inject(MapEntourageFragment fragment);

    MapPresenter getMapPresenter();
}
