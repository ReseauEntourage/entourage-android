package social.entourage.android.tools;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.map.tour.TourService;

public class CrashlyticsNewsFeedListener implements TourService.NewsFeedListener {
    @Override
    public void onNetworkException() {

    }

    @Override
    public void onCurrentPositionNotRetrieved() {

    }

    @Override
    public void onServerException(Throwable throwable) {
        Crashlytics.logException(throwable);
    }

    @Override
    public void onTechnicalException(Throwable throwable) {
        Crashlytics.logException(throwable);
    }

    @Override
    public void onNewsFeedReceived(List<Newsfeed> newsFeeds) {

    }
}
