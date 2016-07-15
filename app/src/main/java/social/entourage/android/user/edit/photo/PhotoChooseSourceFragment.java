package social.entourage.android.user.edit.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

public class PhotoChooseSourceFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.photo_choose_source";

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private static final int READ_STORAGE_PERMISSION_CODE = 3;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 4;

    private static final String KEY_PHOTO_PATH = "social.entourage.android.photo_path";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private PhotoChooseInterface mListener;

    String mCurrentPhotoPath;
    Uri pickedImageUri = null;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PhotoChooseSourceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the photo path
            mCurrentPhotoPath = savedInstanceState.getString(KEY_PHOTO_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_choose_source, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoChooseInterface) {
            mListener = (PhotoChooseInterface) context;
        }
//        else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the photo path
        outState.putString(KEY_PHOTO_PATH, mCurrentPhotoPath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {

            Uri uri = intent.getData();

            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                pickedImageUri = uri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
            } else {
                loadPickedImage(uri);
            }

            return;
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (intent != null && intent.getData() != null ) {
                showNextStep(intent.getData());
                return;
            }
            showNextStep(Uri.fromFile(new File(mCurrentPhotoPath)));
            /*
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, null);
            showNextStep(bitmap);
            */
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_CODE);
                } else {
                    showTakePhotoActivity();
                }
            } else {
                Toast.makeText(getActivity(), R.string.user_photo_error_camera_permission, Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pickedImageUri != null) {
                    loadPickedImage(pickedImageUri);
                }
            } else {
                Toast.makeText(getActivity(), R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showTakePhotoActivity();
            } else {
                Toast.makeText(getActivity(), R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    @OnClick(R.id.photo_choose_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.photo_choose_ignore_button)
    protected void onIgnoreClicked() {

        mListener.onPhotoIgnore();
        dismiss();
    }

    @OnClick(R.id.photo_choose_photo_button)
    protected void onChoosePhotoClicked() {

        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST);
    }

    @OnClick(R.id.photo_choose_take_photo_button)
    protected void onTakePhotoClicked() {

        if (CropImage.isExplicitCameraPermissionRequired(getActivity())) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_CODE);
            } else {
                showTakePhotoActivity();
            }
        }

    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void loadPickedImage(Uri uri) {
        /*
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            showNextStep(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        showNextStep(uri);

        pickedImageUri = null;
    }

    private void showTakePhotoActivity() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            Toast.makeText(getActivity(), R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT).show();
        } else {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getActivity(), R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ENTOURAGE_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showNextStep(Uri photoUri) {
        if (photoUri == null) {
            Toast.makeText(getActivity(), R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        PhotoEditFragment fragment = PhotoEditFragment.newInstance(photoUri);
        fragment.show(getFragmentManager(), PhotoEditFragment.TAG);
    }

}
