package social.entourage.android.map.entourage.create;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * Choose entourage join type {@link EntourageDialogFragment} subclass.
 */
public class CreateEntourageJoinTypeFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = CreateEntourageJoinTypeFragment.class.getSimpleName();

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private CreateEntourageJoinTypeListener listener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageJoinTypeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage_join_type, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    public void setListener(final CreateEntourageJoinTypeListener listener) {
        this.listener = listener;
    }

    // ----------------------------------
    // Interface Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.entourage_join_type_option_private)
    protected void onJoinTypePrivateClicked() {
        if (listener != null) listener.createEntourageWithJoinTypePublic(false);
    }

    @OnClick(R.id.entourage_join_type_option_public)
    protected void onJoinTypePublicClicked() {
        if (listener != null) listener.createEntourageWithJoinTypePublic(true);
    }

    // ----------------------------------
    // Listener
    // ----------------------------------

    public interface CreateEntourageJoinTypeListener {
        void createEntourageWithJoinTypePublic(boolean joinType);
    }

}
