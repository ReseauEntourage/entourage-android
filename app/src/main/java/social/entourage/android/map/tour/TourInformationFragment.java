package social.entourage.android.map.tour;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

public class TourInformationFragment extends DialogFragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    @Bind(R.id.tour_info_organization)
    TextView tourOrganization;

    @Bind(R.id.tour_info_author_photo)
    ImageView tourAuthorPhoto;

    @Bind(R.id.tour_info_type)
    TextView tourType;

    @Bind(R.id.tour_info_author_name)
    TextView tourAuthorName;

    @Bind(R.id.tour_info_discussion_layout)
    LinearLayout discussionLayout;

    @Bind(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    Tour tour;
    List<TourUser> tourUserList;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(Tour tour) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.bind(this, toReturn);
        initializeView();
        return toReturn;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        presenter.getTourUsers();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .tourInformationModule(new TourInformationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof  OnTourInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString() + " must implement OnTourInformationFragmentFinish");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        this.dismiss();
    }


    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);

        tourOrganization.setText(tour.getOrganizationName());

        String type = tour.getTourType();
        if (type != null) {
            if (type.equals(TourType.MEDICAL.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_medical)));
            } else if (type.equals(TourType.ALIMENTARY.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_alimentary)));
            } else if (type.equals(TourType.BARE_HANDS.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_bare_hands)));
            }
        } else {
            tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_info_unknown)));
        }

        tourAuthorName.setText(tour.getAuthor().getUserName());

        String avatarURLAsString = tour.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            ImageLoader.getInstance().loadImage(avatarURLAsString, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
                    tourAuthorPhoto.setImageBitmap(loadedImage);
                }
            });
        }

    }

    private void addDiscussionSeparator() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout separatorLayout = (LinearLayout)inflater.inflate(R.layout.tour_information_separator_card, discussionLayout, false);
        View discussionSeparator = separatorLayout.findViewById(R.id.tic_separator);
        separatorLayout.removeView(discussionSeparator);

        discussionLayout.addView(discussionSeparator);
    }

    private void addDiscussionTourUserCard(TourUser tourUser) {
        TourInformationUserCardView userCardView = new TourInformationUserCardView(getContext());
        userCardView.setUsername(tourUser.getFirstName());
        userCardView.setJoinStatus(tourUser.getStatus());

        discussionLayout.addView(userCardView);
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // Server callbacks
    // ----------------------------------

    protected void onTourUsersReceived(List<TourUser> tourUsers) {
        tourUserList = tourUsers;
        if (tourUserList != null && tourUserList.size() > 1) {
            //order the list based on the request date
            Collections.sort(tourUserList, new TourUser.TourUserComparatorOldToNew());
        }

        //hide the progress bar
        progressBar.setVisibility(View.GONE);

        //add the start time
        TourInformationLocationCardView startCard = new TourInformationLocationCardView(getContext());
        startCard.populate(tour, 0);
        discussionLayout.addView(startCard);

        //add the users
        if (tourUserList != null) {
            for (int i = 0; i < tourUserList.size(); i++) {
                TourUser tourUser = tourUserList.get(i);
                //skip the author
                if (tourUser.getUserId() == tour.getAuthor().getUserID()) {
                    continue;
                }
                //skip the rejected user
                if (tourUser.getStatus().equals(Tour.JOIN_STATUS_REJECTED)) {
                    continue;
                }
                //add the separator
                addDiscussionSeparator();
                //add the user card
                addDiscussionTourUserCard(tourUser);
            }
        }

        //add the end time, if tour is closed
        if (tour.getTourStatus().equals(Tour.TOUR_CLOSED)) {
            addDiscussionSeparator();

            TourInformationLocationCardView endCard = new TourInformationLocationCardView(getContext());
            endCard.populate(tour, tour.getTourPoints().size()-1);
            discussionLayout.addView(endCard);
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void closeTourInformationFragment(TourInformationFragment fragment);
    }
}
