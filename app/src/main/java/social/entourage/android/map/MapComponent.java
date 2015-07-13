package social.entourage.android.map;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MapModule.class
)
public interface MapComponent {
    void inject(MapActivity activity);

    MapPresenter getMapPresenter();
}
