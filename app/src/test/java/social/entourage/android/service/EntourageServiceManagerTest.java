package social.entourage.android.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.api.model.feed.NewsfeedItem;
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemWrapper;
import social.entourage.android.newsfeed.NewsfeedTabItem;
import social.entourage.android.newsfeed.NewsfeedPagination;

@RunWith(HierarchicalContextRunner.class)
public class EntourageServiceManagerTest {
    @Mock private EntourageService service;
    @Mock private ConnectivityManager connectivityManager;
    @Mock private EntourageLocation location;
    @Mock private Context context;
    @InjectMocks private EntourageServiceManager tourServiceManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void retrieveNewsFeed_WithoutNetworkInfo() {
        given(connectivityManager.getActiveNetworkInfo()).willReturn(null);

        tourServiceManager.retrieveNewsFeed(new NewsfeedPagination(), NewsfeedTabItem.ALL_TAB);

        verify(service).notifyListenersNetworkException();
    }

    @Test
    public void retrieveNewsFeed_WithoutConnection() {
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        given(connectivityManager.getActiveNetworkInfo()).willReturn(networkInfo);
        given(networkInfo.isConnected()).willReturn(false);

        tourServiceManager.retrieveNewsFeed(new NewsfeedPagination(), NewsfeedTabItem.ALL_TAB);

        verify(service).notifyListenersNetworkException();
    }

    @Test
    public void retrieveNewsFeed_CurrentPosition() {
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        given(connectivityManager.getActiveNetworkInfo()).willReturn(networkInfo);
        given(networkInfo.isConnected()).willReturn(true);
        given(location.getCurrentCameraPosition()).willReturn(null);

        tourServiceManager.retrieveNewsFeed(new NewsfeedPagination(), NewsfeedTabItem.ALL_TAB);

        verify(service).notifyListenersCurrentPositionNotRetrieved();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public class NewsFeedCallbackTest {
        @Mock private Call<NewsfeedItemWrapper> call;
        @Mock private TourServiceManager manager;
        @Mock private TourService service;
        @Captor private ArgumentCaptor<Throwable> captor;
        @InjectMocks private NewsFeedCallback callback;

        @Before
        public void setup() {
            MockitoAnnotations.initMocks(this);
        }

        public class OnFailure {
            @Test
            public void newsFeedCallback_IfCallIsCancelled() {
                Throwable throwable = new Throwable();
                given(call.isCanceled()).willReturn(true);

                callback.onFailure(call, throwable);

                verify(manager).resetCurrentNewsfeedCall();
                verifyNoMoreInteractions(service);
            }

            @Test
            public void newsFeedCallback() {
                Throwable throwable = new Throwable();

                callback.onFailure(call, throwable);

                verify(manager).resetCurrentNewsfeedCall();
                verify(service).notifyListenersTechnicalException(throwable);
            }
        }

        public class OnResponse {
            @Test
            public void newsFeedCallback_WhenRequestIsCancelled() {
                Response<NewsfeedItemWrapper> response = createServerErrorResponse();
                given(call.isCanceled()).willReturn(true);

                callback.onResponse(call, response);

                verify(manager).resetCurrentNewsfeedCall();
                verifyNoMoreInteractions(EntourageServiceManagerTest.this.service);
            }

            @Test
            public void newsFeedCallback_WithoutServerException() {
                Response<NewsfeedItemWrapper> response = createServerErrorResponse();

                callback.onResponse(call, response);

                verify(manager).resetCurrentNewsfeedCall();
                verify(service).notifyListenersServerException(captor.capture());
                assertThat(captor.getValue().getMessage()).isEqualTo("Response code = 500 : Internal Server Error");
            }

            @Test
            public void newsFeedCallback_WithNullList() {
                NewsfeedItemWrapper wrapper = new NewsfeedItemWrapper();
                wrapper.newsfeedItems = null;
                Response<NewsfeedItemWrapper> response = Response.success(wrapper);

                callback.onResponse(call, response);

                verify(manager).resetCurrentNewsfeedCall();
                verify(service).notifyListenersTechnicalException(captor.capture());
                assertThat(captor.getValue().getMessage()).isEqualTo("Null newsfeed list");
            }

            @Test
            public void newsFeedCallback() {
                List<NewsfeedItem> newsFeeds = Collections.singletonList(mock(NewsfeedItem.class));
                NewsfeedItemWrapper wrapper = new NewsfeedItemWrapper();
                wrapper.newsfeedItems = newsFeeds;
                Response<NewsfeedItemWrapper> response = Response.success(wrapper);

                callback.onResponse(call, response);

                verify(manager).resetCurrentNewsfeedCall();
                verify(service).notifyListenersNewsFeedReceived(newsFeeds);
            }

            private Response<NewsfeedItemWrapper> createServerErrorResponse() {
                ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "Internal Server Error");
                return Response.error(500, body);
            }
        }
    }
}