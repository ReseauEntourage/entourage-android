package social.entourage.android.carousel;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;

/**
 * Help carousel
 */
public class CarouselFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.carousel";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.carousel_close_button)
    Button closeButton;

    @BindView(R.id.carousel_view)
    ViewPager pagerView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public CarouselFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_carousel, container, false);

        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeCarousel();
    }

    private void initializeCarousel() {
        CarouselPageAdapter pageAdapter = new CarouselPageAdapter(getChildFragmentManager());
        pagerView.setAdapter(pageAdapter);
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                closeButton.setVisibility(position == CarouselPageAdapter.NUM_PAGES - 1 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.carousel_close_button)
    public void onCloseClicked() {
        dismiss();
    }

}
