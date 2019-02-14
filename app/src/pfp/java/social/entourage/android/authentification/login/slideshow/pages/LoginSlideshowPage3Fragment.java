package social.entourage.android.authentification.login.slideshow.pages;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import social.entourage.android.R;

/**
 * Login slideshow page 3 {@link Fragment}.
 */
public class LoginSlideshowPage3Fragment extends Fragment {


    public LoginSlideshowPage3Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_slideshow_page3, container, false);
    }

}
