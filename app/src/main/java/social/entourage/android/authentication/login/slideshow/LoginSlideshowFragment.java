package social.entourage.android.authentication.login.slideshow;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.authentification.login.slideshow.LoginSlideshowPageAdapter;

import static social.entourage.android.authentification.login.slideshow.LoginSlideshowPageAdapter.NUM_PAGES;

/**
 * Login slideshow {@link Fragment} subclass.
 */
public class LoginSlideshowFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = LoginSlideshowFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.login_slideshow_view)
    ViewPager pagerView;

    @Nullable
    @BindView(R.id.login_slideshow_indicator_layout)
    LinearLayout indicatorLayout;

    List<ImageView> dots;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public LoginSlideshowFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.login_slideshow, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeCarousel();
    }

    private void initializeCarousel() {
        LoginSlideshowPageAdapter pageAdapter = new LoginSlideshowPageAdapter(getChildFragmentManager());
        pagerView.setAdapter(pageAdapter);
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                EntourageEvents.logEvent(Constants.EVENT_LOGIN_SLIDESHOW);
                selectDot(position);
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        addDots();
    }

    private void addDots() {
        if (indicatorLayout == null) return;
        dots = new ArrayList<>();

        ImageView dot1 = indicatorLayout.findViewById(R.id.slideshow_b1);
        dots.add(dot1);

        ImageView dot2 = indicatorLayout.findViewById(R.id.slideshow_b2);
        dots.add(dot2);

        ImageView dot3 = indicatorLayout.findViewById(R.id.slideshow_b3);
        dots.add(dot3);
    }

    public void selectDot(int idx) {
        if (dots == null || dots.size() <= idx) return;
        Resources res = getResources();
        for(int i = 0; i < NUM_PAGES; i++) {
            int drawableId = (i==idx)?(R.drawable.carousel_bullet_filled):(R.drawable.carousel_bullet_empty);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }

}