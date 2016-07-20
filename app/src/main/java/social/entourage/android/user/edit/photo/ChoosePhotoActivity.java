package social.entourage.android.user.edit.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class ChoosePhotoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {

            Uri uri = intent.getData();

            BusProvider.getInstance().post(new Events.OnPhotoChosen(uri));

//            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                pickedImageUri = uri;
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
//            } else {
//                loadPickedImage(uri);
//            }

        }

        finish();

    }
}
