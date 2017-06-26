package social.entourage.android.map.entourage;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.base.EntourageDialogFragment;

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

    private String entourageDescription;
    private String entourageType;

    private CreateEntourageListener mListener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageDescriptionFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageDescriptionFragment newInstance(String description, String entourageType) {
        CreateEntourageDescriptionFragment fragment = new CreateEntourageDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ENTOURAGE_DESCRIPTION, description);
        args.putString(CreateEntourageFragment.KEY_ENTOURAGE_TYPE, entourageType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entourageDescription = getArguments().getString(ENTOURAGE_DESCRIPTION);
            entourageType = getArguments().getString(CreateEntourageFragment.KEY_ENTOURAGE_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage_description, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
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

    @OnClick(R.id.description_entourage_close)
    void onCloseClicked() {
        mListener = null;
        dismiss();
    }

    @OnClick(R.id.description_entourage_validate_button)
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
        descriptionEditText.setHint(Entourage.TYPE_CONTRIBUTION.equals(entourageType) ? R.string.entourage_create_description_contribution_hint : R.string.entourage_create_description_demand_hint);
    }

}
