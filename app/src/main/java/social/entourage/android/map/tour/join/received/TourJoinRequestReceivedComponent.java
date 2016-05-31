package social.entourage.android.map.tour.join.received;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Created by mihaiionescu on 18/03/16.
 */

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = TourJoinRequestReceivedModule.class
)

public interface TourJoinRequestReceivedComponent {
    void inject(TourJoinRequestReceivedActivity activity);

    TourJoinRequestReceivedPresenter getJoinRequestReceivedPresenter();
}
