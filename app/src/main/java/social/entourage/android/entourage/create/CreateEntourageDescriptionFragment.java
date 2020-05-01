package social.entourage.android.entourage.create;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.entourage.category.EntourageCategory;
import social.entourage.android.entourage.category.EntourageCategoryFragment;

/**
 * Dialog Fragment for editing an entourage entourageDescription
 */
public class CreateEntourageDescriptionFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = CreateEntourageDescriptionFragment.class.getSimpleName();

    private static final String ENTOURAGE_DESCRIPTION = "ENTOURAGE_DESCRIPTION";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.description_entourage_edittext)
    EditText descriptionEditText;

    @BindView(R.id.description_entourage_info_text)
    TextView infoTextView;

    private String entourageDescription;
    private EntourageCategory entourageCategory;
    private String entourageGroupType;

    private CreateEntourageListener mListener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageDescriptionFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageDescriptionFragment newInstance(String description, EntourageCategory entourageCategory, String groupType) {
        CreateEntourageDescriptionFragment fragment = new CreateEntourageDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ENTOURAGE_DESCRIPTION, description);
        args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, entourageCategory);
        args.putString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE, groupType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            entourageDescription = args.getString(ENTOURAGE_DESCRIPTION);
            entourageCategory = (EntourageCategory)args.getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY);
            entourageGroupType = args.getString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage_description, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showKeyboard();
    }

    public void setListener(final CreateEntourageListener mListener) {
        this.mListener = mListener;
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
            mListener.onDescriptionChanged(descriptionEditText.getText().toString());
        }
        mListener = null;
        dismiss();
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initializeView() {
        if (entourageDescription != null) {
            descriptionEditText.setText(entourageDescription);
            descriptionEditText.setSelection(entourageDescription.length());
        }
        if (entourageCategory != null) {
            String descriptionExample = entourageCategory.descriptionExample;
            if (descriptionExample != null && descriptionExample.length() > 0) {
                descriptionEditText.setHint(descriptionExample);
            }
        }
        if (BaseEntourage.GROUPTYPE_OUTING.equalsIgnoreCase(entourageGroupType)) {
            descriptionEditText.setHint(R.string.entourage_description_fragment_hint_outing);
            infoTextView.setText(R.string.entourage_description_fragment_info_outing);
        }
    }

}
