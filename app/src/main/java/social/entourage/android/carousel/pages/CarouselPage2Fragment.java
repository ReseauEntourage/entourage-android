package social.entourage.android.carousel.pages;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import social.entourage.android.R;

/**
 * Carousel Page 2
 */
public class CarouselPage2Fragment extends Fragment {


    public CarouselPage2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_carousel_page2, container, false);

        return v;
    }

}
