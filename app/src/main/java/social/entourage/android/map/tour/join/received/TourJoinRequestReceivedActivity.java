package social.entourage.android.map.tour.join.received;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.zip.Inflater;

import javax.inject.Inject;

import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.message.push.PushNotificationService;
import social.entourage.android.user.UserFragment;
import social.entourage.android.view.HtmlTextView;

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
        View view = getLayoutInflater().inflate(R.layout.layout_tour_join_request_received_dialog, null);
        HtmlTextView htmlTextView =  (HtmlTextView)view.findViewById(R.id.tour_join_request_received_text);
        htmlTextView.setHtmlString(getString(R.string.tour_join_request_received_message_html, message.getAuthor()));
        htmlTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showUserProfile();
            }
        });
        builder.setView(view)
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
                        PushNotificationContent content = message.getContent();
                        if (content != null) {
                            presenter.rejectJoinRequest(message.getContent().getTourId(), message.getContent().getUserId());
                        }
                    }
                });
        builder.show();
    }

    protected void onUserTourStatusChanged(boolean statusChanged) {
        if (statusChanged == false) {
            Toast.makeText(this, R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
            displayMessage();
        }
        else {
            Toast toast = Toast.makeText(this, R.string.tour_join_request_success, Toast.LENGTH_SHORT);
            int duration = toast.getDuration();
            toast.show();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, duration+100);
        }
    }

    private void showUserProfile() {
        PushNotificationContent content = message.getContent();
        if (content == null) {
            return;
        }
        UserFragment fragment = UserFragment.newInstance(content.getUserId());
        fragment.show(getSupportFragmentManager(), UserFragment.TAG);
    }

}
