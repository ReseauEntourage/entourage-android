package social.entourage.android.map.tour.my;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;
import social.entourage.android.map.tour.information.TourInformationModule;

/**
 * Created by mihaiionescu on 10/03/16.
 */

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MyToursModule.class
)
public interface MyToursComponent {
    void inject(MyToursFragment fragment);

    MyToursPresenter getMyToursPresenter(MyToursPresenter presenter);
}
