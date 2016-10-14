package social.entourage.android.user.edit.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class TakePhotoActivity extends AppCompatActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int REQUEST_TAKE_PHOTO = 2;

    public static final String KEY_PHOTO_PATH = "social.entourage.android.photo_path";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    String mCurrentPhotoPath;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the take photo intent
        if (getIntent() != null) {
            mCurrentPhotoPath = getIntent().getStringExtra(KEY_PHOTO_PATH);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (getIntent().getData() != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        getIntent().getData());
            }
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (intent != null && intent.getData() != null ) {
                BusProvider.getInstance().post(new Events.OnPhotoChosen(intent.getData()));
                return;
            }
            if (mCurrentPhotoPath != null) {
                BusProvider.getInstance().post(new Events.OnPhotoChosen(Uri.fromFile(new File(mCurrentPhotoPath))));
            }
            finish();
        }
    }
}
