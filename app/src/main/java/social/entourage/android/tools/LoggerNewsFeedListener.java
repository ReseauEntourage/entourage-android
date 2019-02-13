package social.entourage.android.tools;

import android.util.Log;

import java.util.List;

import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.map.tour.TourService;

public class LoggerNewsFeedListener implements TourService.NewsFeedListener {
    private static final String TAG = LoggerNewsFeedListener.class.getSimpleName();

    @Override
    public void onNetworkException() {
        Log.e(TAG, "Network exception");
    }

    @Override
    public void onCurrentPositionNotRetrieved() {
        Log.e(TAG, "Current position not retrieved");
    }

    @Override
    public void onServerException(Throwable throwable) {
        Log.e(TAG, "Server exception", throwable);
    }

    @Override
    public void onTechnicalException(Throwable throwable) {
        Log.e(TAG, "Technical exception", throwable);
    }

    @Override
    public void onNewsFeedReceived(List<Newsfeed> newsFeeds) {
        Log.d(TAG, "NewsFeed received, size = " + newsFeeds.size());
    }
}
