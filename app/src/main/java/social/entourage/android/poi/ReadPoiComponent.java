package social.entourage.android.poi;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ReadPoiModule.class
)
public interface ReadPoiComponent {
    void inject(ReadPoiActivity activity);

    ReadPoiPresenter getReadPoiPresenter();
}
