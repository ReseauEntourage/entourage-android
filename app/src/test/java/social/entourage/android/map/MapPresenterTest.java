package social.entourage.android.map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MapPresenterTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @Mock private MapEntourageFragment fragment;
    @Mock private AuthenticationController controller;
    @Mock private User user;
    @InjectMocks private MapPresenter presenter;

    @Test
    public void handleGeolocationPermission_WhenUserIsNull() {
        given(controller.getUser()).willReturn(null);

        presenter.handleLocationPermission();

        verify(fragment).checkPermission(ACCESS_COARSE_LOCATION);
    }

    @Test
    public void handleGeolocationPermission_WhenUserIsPublic() {
        given(user.isPro()).willReturn(false);
        given(controller.getUser()).willReturn(user);

        presenter.handleLocationPermission();

        verify(fragment).checkPermission(ACCESS_COARSE_LOCATION);
    }

    @Test
    public void handleGeolocationPermission_WhenUserIsPro() {
        User user = mock(User.class);
        given(user.isPro()).willReturn(true);
        given(controller.getUser()).willReturn(user);

        presenter.handleLocationPermission();

        verify(fragment).checkPermission(ACCESS_FINE_LOCATION);
    }
}