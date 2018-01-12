package social.entourage.android.carousel.pages;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.carousel.CarouselFragment;
import social.entourage.android.user.edit.partner.UserEditPartnerFragment;

/**
 * Carousel page 4
 */
public class CarouselPage4Fragment extends Fragment {


    public CarouselPage4Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_carousel_page4, container, false);

        ButterKnife.bind(this, view);

        return view;
    }
}
