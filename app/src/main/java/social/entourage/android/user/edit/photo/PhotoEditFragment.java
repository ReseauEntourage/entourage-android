package social.entourage.android.user.edit.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;

public class PhotoEditFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.photo_edit";

    private static final String PHOTO_PARAM = "social.entourage.android.photo_param";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private PhotoChooseInterface mListener;

    private Uri photoUri;

    @Bind(R.id.photo_edit_cropImageView)
    CropImageView cropImageView;

    @Bind(R.id.photo_edit_fab_button)
    FloatingActionButton fabButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PhotoEditFragment() {
        // Required empty public constructor
    }

    public static PhotoEditFragment newInstance(Uri photoUri) {
        PhotoEditFragment fragment = new PhotoEditFragment();
        Bundle args = new Bundle();
        /*
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        args.putByteArray(PHOTO_PARAM, byteArray);
        */
        args.putParcelable(PHOTO_PARAM, photoUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            /*
            byte[] byteArray = getArguments().getByteArray(PHOTO_PARAM);
            if (byteArray != null) {
                photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            }
            */
            photoUri = getArguments().getParcelable(PHOTO_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_edit, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (photoUri != null) {
            //cropImageView.setImageBitmap(photo);
            cropImageView.setImageUriAsync(photoUri);
        }
        cropImageView.setCropShape(CropImageView.CropShape.OVAL);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoChooseInterface) {
            mListener = (PhotoChooseInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    @OnClick(R.id.photo_edit_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.photo_edit_fab_button)
    protected void onOkClicked() {
        fabButton.setEnabled(false);
        cropImageView.setOnSaveCroppedImageCompleteListener(new CropImageView.OnSaveCroppedImageCompleteListener() {
            @Override
            public void onSaveCroppedImageComplete(final CropImageView view, final Uri uri, final Exception error) {
                if (error != null) {
                    Log.d("PhotoEdit", error.getMessage());
                    Toast.makeText(getActivity(), R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show();
                    fabButton.setEnabled(true);
                    return;
                }
                mListener.onPhotoChosen(uri);
            }
        });
        try {
            File croppedImageFile = createImageFile();
            cropImageView.saveCroppedImageAsync(Uri.fromFile(croppedImageFile));
        } catch (IOException e) {
            Toast.makeText(getActivity(), R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // Upload handling
    // ----------------------------------

    public boolean onPhotoSent(boolean success) {
        if (success && !fabButton.isEnabled()) {
            //getContext().getContentResolver().delete(photoUri, "", null);
            dismiss();
            return true;
        } else {
            fabButton.setEnabled(true);
        }
        return false;
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ENTOURAGE_CROP_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

}
