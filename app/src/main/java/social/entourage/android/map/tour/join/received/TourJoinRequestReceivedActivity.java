package social.entourage.android.map.tour.join.received;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;

import javax.inject.Inject;

import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.message.push.PushNotificationService;

public class TourJoinRequestReceivedActivity extends EntourageSecuredActivity {

    private Message message;

    @Inject
    TourJoinRequestReceivedPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_join_request_received);

        message = (Message)getIntent().getExtras().getSerializable(PushNotificationService.PUSH_MESSAGE);
        if (message != null) {
            displayMessage();
        }
    }

    @Override
    protected void setupComponent(final EntourageComponent entourageComponent) {
        DaggerTourJoinRequestReceivedComponent.builder()
                .entourageComponent(entourageComponent)
                .tourJoinRequestReceivedModule(new TourJoinRequestReceivedModule(this))
                .build()
                .inject(this);
    }

    private void displayMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message.getMessage())
                .setCancelable(false)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        PushNotificationContent content = message.getContent();
                        if (content != null) {
                            presenter.acceptJoinRequest(message.getContent().getTourId(), message.getContent().getUserId());
                        }
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        finish();
                    }
                });
        builder.show();
    }

    protected void onUserTourStatusChanged(TourUser user) {
        if (user == null) {

        }
        else {

        }
    }

}
