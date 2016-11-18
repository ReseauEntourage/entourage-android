package social.entourage.android.map.tour;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.Newsfeed.NewsfeedWrapper;
import social.entourage.android.map.tour.TourServiceManager.NewsFeedCallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

@RunWith(HierarchicalContextRunner.class)
public class TourServiceManagerTest {
    @Mock private TourService tourService;
    @Mock private ConnectivityManager connectivityManager;
    @Mock private EntourageLocation location;
    @Mock private Context context;
    @InjectMocks private TourServiceManager tourServiceManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void retrieveNewsFeed_WithoutNetworkInfo() {
        given(connectivityManager.getActiveNetworkInfo()).willReturn(null);

        tourServiceManager.retrieveNewsFeed(new Date(),context);

        verify(tourService).notifyListenersNetworkException();
    }

    @Test
    public void retrieveNewsFeed_WithoutConnection() {
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        given(connectivityManager.getActiveNetworkInfo()).willReturn(networkInfo);
        given(networkInfo.isConnected()).willReturn(false);

        tourServiceManager.retrieveNewsFeed(new Date(),context);

        verify(tourService).notifyListenersNetworkException();
    }

    @Test
    public void retrieveNewsFeed_CurrentPosition() {
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        given(connectivityManager.getActiveNetworkInfo()).willReturn(networkInfo);
        given(networkInfo.isConnected()).willReturn(true);
        given(location.getCurrentCameraPosition()).willReturn(null);

        tourServiceManager.retrieveNewsFeed(new Date(),context);

        verify(tourService).notifyListenersCurrentPositionNotRetrieved();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public class NewsFeedCallbackTest {
        @Mock private Call<NewsfeedWrapper> call;
        @Mock private TourService tourService;
        @Captor private ArgumentCaptor<Throwable> captor;
        @InjectMocks private NewsFeedCallback callback;

        @Before
        public void setup() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void newsFeedCallback_OnFailure() {
            Throwable throwable = new Throwable();

            callback.onFailure(call, throwable);

            verify(tourService).notifyListenersTechnicalException(throwable);
        }

        @Test
        public void newsFeedCallback_OnResponse_WithoutServerException() {
            ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "Internal Server Error");
            Response<NewsfeedWrapper> response = Response.error(500, body);

            callback.onResponse(call, response);

            verify(tourService).notifyListenersServerException(captor.capture());
            assertThat(captor.getValue().getMessage()).isEqualTo("Response code = 500 : Internal Server Error");
        }

        @Test
        public void newsFeedCallback_OnResponse_WithNullList() {
            NewsfeedWrapper wrapper = new NewsfeedWrapper();
            wrapper.setNewsfeed(null);
            Response<NewsfeedWrapper> response = Response.success(wrapper);

            callback.onResponse(call, response);

            verify(tourService).notifyListenersTechnicalException(captor.capture());
            assertThat(captor.getValue().getMessage()).isEqualTo("Null newsfeed list");
        }

        @Test
        public void newsFeedCallback_OnResponse() {
            List<Newsfeed> newsFeeds = Collections.singletonList(mock(Newsfeed.class));
            NewsfeedWrapper wrapper = new NewsfeedWrapper();
            wrapper.setNewsfeed(newsFeeds);
            Response<NewsfeedWrapper> response = Response.success(wrapper);

            callback.onResponse(call, response);

            verify(tourService).notifyListenersNewsFeedReceived(newsFeeds);
        }
    }
}