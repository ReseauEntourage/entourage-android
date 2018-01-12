package social.entourage.android.carousel.pages;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.R;

/**
 * Carousel page 1
 */
public class CarouselPage1Fragment extends Fragment {


    public CarouselPage1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_carousel_page1, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

}
