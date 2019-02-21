package social.entourage.android.carousel.pages;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import social.entourage.android.R;

/**
 * Carousel page 1
 */
public class CarouselPage1Fragment extends Fragment {


    public CarouselPage1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_carousel_page1, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

}
