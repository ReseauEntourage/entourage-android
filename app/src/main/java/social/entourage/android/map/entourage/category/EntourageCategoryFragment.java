package social.entourage.android.map.entourage.category;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.map.entourage.CreateEntourageListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link social.entourage.android.map.entourage.CreateEntourageListener} interface
 * to handle interaction events.
 * Use the {@link EntourageCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntourageCategoryFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = EntourageCategoryFragment.class.getSimpleName();

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private static final String ENTOURAGE_TYPE = "ENTOURAGE_TYPE";
    private static final String ENTOURAGE_CATEGORY = "ENTOURAGE_CATEGORY";

    private String entourageType;
    private String category;

    private CreateEntourageListener mListener;

    public EntourageCategoryFragment() {
        // Required empty public constructor
    }

    public static EntourageCategoryFragment newInstance(String entourageType, String category) {
        EntourageCategoryFragment fragment = new EntourageCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ENTOURAGE_TYPE, entourageType);
        args.putString(ENTOURAGE_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public static EntourageCategoryFragment newInstance() {
        EntourageCategoryFragment fragment = new EntourageCategoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entourageType = getArguments().getString(ENTOURAGE_TYPE);
            category = getArguments().getString(ENTOURAGE_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_category, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentFromRight;
    }

    public void setListener(final CreateEntourageListener mListener) {
        this.mListener = mListener;
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------

    @OnClick(R.id.entourage_category_close)
    void onCloseClicked() {
        mListener = null;
        dismiss();
    }

    @OnClick(R.id.entourage_category_validate_button)
    void onValidateClicked() {
        if (mListener != null) {
            mListener.onCategoryChosen(entourageType, category);
        }
        mListener = null;
        dismiss();
    }

}
