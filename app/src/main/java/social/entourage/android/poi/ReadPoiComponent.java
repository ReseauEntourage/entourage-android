package social.entourage.android.poi;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to ReadPoiActivity lifecycle
 * Provide a ReadPoiPresenter
 * @see ReadPoiActivity
 * @see ReadPoiPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ReadPoiModule.class
)
@SuppressWarnings("unused")
public interface ReadPoiComponent {
    void inject(ReadPoiActivity activity);

    ReadPoiPresenter getReadPoiPresenter();
}
