package social.entourage.android.entourage.category;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.MainActivity;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.entourage.create.CreateEntourageListener;
import social.entourage.android.view.HtmlTextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateEntourageListener} interface
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

    public static final String KEY_ENTOURAGE_CATEGORY = "ENTOURAGE_CATEGORY";
    public static final String KEY_ENTOURAGE_ISDEMAND = "ENTOURAGE_ISDEMAND";

    private EntourageCategory category;

    private CreateEntourageListener mListener;

    @BindView(R.id.entourage_category_listview)
    ExpandableListView listView;

    private EntourageCategoriesAdapter adapter;

    private boolean isDemand;

    public EntourageCategoryFragment() {
        // Required empty public constructor
    }

    public static EntourageCategoryFragment newInstance(EntourageCategory category,boolean isDemand) {
        EntourageCategoryFragment fragment = new EntourageCategoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_ENTOURAGE_CATEGORY, category);
        args.putBoolean(KEY_ENTOURAGE_ISDEMAND, isDemand);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_category, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            category = (EntourageCategory)getArguments().getSerializable(KEY_ENTOURAGE_CATEGORY);
            if (category != null && !category.isNewlyCreated()) {
                category.setSelected(true);
            } else {
                category = EntourageCategoryManager.getDefaultCategory();
                category.setNewlyCreated(true);
                category.setSelected(false);
            }
            isDemand = getArguments().getBoolean(KEY_ENTOURAGE_ISDEMAND,true);
        }

        initializeView();
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        resetSelectedCategory();
        super.onDismiss(dialog);
    }

    @Override
    public void dismiss() {
        resetSelectedCategory();
        super.dismiss();
    }

    private void resetSelectedCategory() {
        if (adapter != null && adapter.selectedCategory != null) {
            // Reset the flag so consequent fragment shows will not appear broken
            adapter.selectedCategory.setSelected(false);
        }
    }

    public void setListener(final CreateEntourageListener mListener) {
        this.mListener = mListener;
    }

    private void initializeView() {
        initializeListView();
        initializeHelpHtmlView();
    }

    private void initializeListView() {
        HashMap<String, List<EntourageCategory>> entourageCategoryHashMap = EntourageCategoryManager.getEntourageCategories();
        List<String> groupTypeList = EntourageCategoryManager.getGroupTypes();
        adapter = new EntourageCategoriesAdapter(getContext(), groupTypeList, entourageCategoryHashMap, category, isDemand);
        listView.setAdapter(adapter);
        int count = adapter.getGroupCount();
        for (int position = 0; position < count; position++) {
            listView.expandGroup(position);
        }
        //Disable click on group header
        listView.setOnGroupClickListener((expandableListView, view, i, l) -> true);
    }

    private void initializeHelpHtmlView() {
        HtmlTextView helpHtmlTextView = getView().findViewById(R.id.entourage_category_help_link);
        if (helpHtmlTextView != null) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                String goalLink = ((MainActivity) getActivity()).getLink(Constants.GOAL_LINK_ID);
                helpHtmlTextView.setHtmlString(getString(R.string.entourage_create_help_text, goalLink), EntourageLinkMovementMethod.getInstance());
            }
        }
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    void onCloseClicked() {
        mListener = null;
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    void onValidateClicked() {
        if (mListener != null) {
            mListener.onCategoryChosen(adapter.selectedCategory);
        }
        mListener = null;
        dismiss();
    }

}
