package social.entourage.android.authentication.login.slideshow;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Login Slideshow page 1 {@link Fragment}.
 */
public class LoginSlideshowPageFragment extends Fragment {

    private int layout;

    public LoginSlideshowPageFragment(int page_layout) {
        super();
        layout = page_layout;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false);
    }

}
