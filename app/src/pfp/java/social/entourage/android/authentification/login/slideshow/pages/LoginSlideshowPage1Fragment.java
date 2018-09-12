package social.entourage.android.authentification.login.slideshow.pages;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import social.entourage.android.R;

/**
 * Login slideshow page 1 {@link Fragment}.
 */
public class LoginSlideshowPage1Fragment extends Fragment {


    public LoginSlideshowPage1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_slideshow_page1, container, false);
    }

}
