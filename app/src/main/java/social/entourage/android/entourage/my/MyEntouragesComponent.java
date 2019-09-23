package social.entourage.android.entourage.my;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Created by mihaiionescu on 03/08/16.
 */

@ActivityScope
@Component (
        dependencies = EntourageComponent.class,
        modules = MyEntouragesModule.class
)

public interface MyEntouragesComponent {
    void inject(MyEntouragesFragment fragment);

    MyEntouragesPresenter getMyEntouragesPresenter(MyEntouragesPresenter presenter);
}
