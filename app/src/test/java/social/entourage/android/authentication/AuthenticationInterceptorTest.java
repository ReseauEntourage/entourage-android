package social.entourage.android.authentication;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import retrofit.RequestInterceptor;
import social.entourage.android.api.model.User;

public class AuthenticationInterceptorTest {

    @Module(
            injects = AuthenticationInterceptorTest.class,
            includes = AuthenticationModule.class,
            overrides = true,
            complete = false
    )
    static class AuthenticatorInterceptorTestModule {

        @Provides
        @Singleton
        AuthenticationController providesAuthenticationController() {
            return Mockito.mock(AuthenticationController.class);
        }
    }

    @Inject
    AuthenticationController authenticationController;
    @Inject
    AuthenticationInterceptor authenticationInterceptor;

    @Before
    public void setUp() throws Exception {
        final AuthenticatorInterceptorTestModule authenticatorInterceptorTestModule = new AuthenticatorInterceptorTestModule();
        ObjectGraph.create(authenticatorInterceptorTestModule).inject(this);
    }

    @Test
    public void testIntercept_authenticated() throws Exception {
        final RequestInterceptor.RequestFacade requestFacade = Mockito.mock(RequestInterceptor.RequestFacade.class);
        Mockito.when(authenticationController.isAuthenticated()).thenReturn(true);
        final User user = Mockito.mock(User.class);
        Mockito.when(user.getToken()).thenReturn("token");
        Mockito.when(authenticationController.getUser()).thenReturn(user);

        authenticationInterceptor.intercept(requestFacade);

        Mockito.verify(requestFacade).addEncodedQueryParam("token", "token");
    }

    @Test
    public void testIntercept_notAuthenticated() throws Exception {
        final RequestInterceptor.RequestFacade requestFacade = Mockito.mock(RequestInterceptor.RequestFacade.class);
        Mockito.when(authenticationController.isAuthenticated()).thenReturn(false);
        authenticationInterceptor.intercept(requestFacade);
    }
}