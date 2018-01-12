package social.entourage.android.carousel;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;

import static social.entourage.android.carousel.CarouselPageAdapter.NUM_PAGES;

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

    @BindView(R.id.carousel_indicator_layout)
    LinearLayout indicatorLayout;

    List<ImageView> dots;

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
                selectDot(position);
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        addDots();
    }

    private void addDots() {
        dots = new ArrayList<>();

        ImageView dot1 = (ImageView)indicatorLayout.findViewById(R.id.carousel_b1);
        dots.add(dot1);

        ImageView dot2 = (ImageView)indicatorLayout.findViewById(R.id.carousel_b2);
        dots.add(dot2);

        ImageView dot3 = (ImageView)indicatorLayout.findViewById(R.id.carousel_b3);
        dots.add(dot3);

        ImageView dot4 = (ImageView)indicatorLayout.findViewById(R.id.carousel_b4);
        dots.add(dot4);
    }

    public void selectDot(int idx) {
        Resources res = getResources();
        for(int i = 0; i < NUM_PAGES; i++) {
            int drawableId = (i==idx)?(R.drawable.carousel_bullet_filled):(R.drawable.carousel_bullet_empty);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.carousel_close_button)
    public void onCloseClicked() {
        dismiss();
    }

}
