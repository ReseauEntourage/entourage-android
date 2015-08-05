package social.entourage.android.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;

public class UserEntourageFragment extends Fragment {

    @Inject
    UserPresenter presenter;

    @InjectView(R.id.user_photo)
    View userPhoto;

    @InjectView(R.id.user_name)
    TextView userName;

    @InjectView(R.id.user_email)
    TextView userEmail;

    @InjectView(R.id.user_tours_count)
    TextView userTourCount;

    @InjectView(R.id.user_encounters_count)
    TextView userEncountersCount;

    @InjectView(R.id.user_association)
    TextView userAssociation;

    @InjectView(R.id.user_button_change_photo)
    Button buttonPhoto;

    @InjectView(R.id.user_button_change_email)
    Button buttonEmail;

    @InjectView(R.id.user_button_change_password)
    Button buttonPassword;

    @InjectView(R.id.user_button_unsubscribe)
    Button buttonUnsubscribe;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.inject(this, toReturn);

        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerUserComponent.builder()
                .entourageComponent(entourageComponent)
                .userModule(new UserModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.activity_display_user_title);
    }

    @OnClick(R.id.user_button_change_photo)
    void changePhoto() {

    }

    @OnClick(R.id.user_button_change_email)
    void changeEmail() {

    }

    @OnClick(R.id.user_button_change_password)
    void changePassword() {

    }

    @OnClick(R.id.user_button_unsubscribe)
    void unsubscribe() {

    }
}
