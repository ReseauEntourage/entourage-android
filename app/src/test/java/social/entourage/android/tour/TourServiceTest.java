package social.entourage.android.tour;

import java.util.Collections;
import java.util.List;

import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.newsfeed.NewsFeedListener;

import static social.entourage.android.tour.TourService.NewsFeedListener;

@RunWith(HierarchicalContextRunner.class)
public class TourServiceTest {
    private final NewsFeedListener listener = mock(NewsFeedListener.class);
    private final TourService service = new TourService();

    public class AfterNewsFeedListenerRegistration {
        @Before
        public void registerListener() {
            service.registerNewsFeedListener(listener);
        }

        @Test
        public void notifyListenerNetworkException() {
            service.notifyListenersNetworkException();

            verify(listener).onNetworkException();
        }

        @Test
        public void notifyListenersCurrentPositionNotRetrieved() {
            service.notifyListenersCurrentPositionNotRetrieved();

            verify(listener).onCurrentPositionNotRetrieved();
        }

        @Test
        public void notifyListenersServerException() {
            Throwable throwable = new Throwable();

            service.notifyListenersServerException(throwable);

            verify(listener).onServerException(throwable);
        }

        @Test
        public void notifyListenersTechnicalException() {
            Throwable throwable = new Throwable();

            service.notifyListenersTechnicalException(throwable);

            verify(listener).onTechnicalException(throwable);
        }

        @Test
        public void notifyListenersNewsFeedReceived() {
            List<Newsfeed> newsFeeds = Collections.emptyList();

            service.notifyListenersNewsFeedReceived(newsFeeds);

            verify(listener).onNewsFeedReceived(newsFeeds);
        }

        public class AfterNewsFeedListenerUnRegistration {
            @Before
            public void unregisterListener() {
                service.unregisterNewsFeedListener(listener);
            }

            @Test
            public void notifyListenerNetworkException() {
                service.notifyListenersNetworkException();

                verify(listener, never()).onNetworkException();
            }

            @Test
            public void notifyListenersCurrentPositionNotRetrieved() {
                service.notifyListenersCurrentPositionNotRetrieved();

                verify(listener, never()).onCurrentPositionNotRetrieved();
            }

            @Test
            public void notifyListenersServerException() {
                Throwable throwable = new Throwable();

                service.notifyListenersServerException(throwable);

                verify(listener, never()).onServerException(throwable);
            }

            @Test
            public void notifyListenersTechnicalException() {
                Throwable throwable = new Throwable();

                service.notifyListenersTechnicalException(throwable);

                verify(listener, never()).onTechnicalException(throwable);
            }

            @Test
            public void notifyListenersNewsFeedReceived() {
                List<Newsfeed> newsFeeds = Collections.emptyList();

                service.notifyListenersNewsFeedReceived(newsFeeds);

                verify(listener, never()).onNewsFeedReceived(newsFeeds);
            }
        }
    }
}