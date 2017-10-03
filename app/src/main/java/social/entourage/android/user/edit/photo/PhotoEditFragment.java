package social.entourage.android.user.edit.photo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

public class PhotoEditFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.photo_edit";

    private static final String PHOTO_PARAM = "social.entourage.android.photo_param";
    private static final String PHOTO_SOURCE = "social.entourage.android.photo_source";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @BindView(R.id.photo_edit_cropImageView)
    CropImageView cropImageView;
    @BindView(R.id.photo_edit_fab_button)
    FloatingActionButton fabButton;
    private PhotoChooseInterface mListener;
    private Uri photoUri;
    private int photoSource;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PhotoEditFragment() {
        // Required empty public constructor
    }

    public static PhotoEditFragment newInstance(Uri photoUri, int photoSource) {
        PhotoEditFragment fragment = new PhotoEditFragment();
        Bundle args = new Bundle();
        args.putParcelable(PHOTO_PARAM, photoUri);
        args.putInt(PHOTO_SOURCE, photoSource);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoUri = getArguments().getParcelable(PHOTO_PARAM);
            photoSource = getArguments().getInt(PHOTO_SOURCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_09_9);
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
        cropImageView.setAspectRatio(1, 1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    public boolean onPhotoSent(boolean success) {
        if (success && !fabButton.isEnabled() && !isStopped()) {
            //getContext().getContentResolver().delete(photoUri, "", null);
            dismiss();
            return true;
        } else {
            fabButton.setEnabled(true);
        }
        return false;
    }

    @OnClick(R.id.photo_edit_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    // ----------------------------------
    // Upload handling
    // ----------------------------------

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
                mListener.onPhotoChosen(uri, photoSource);
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
    // Private methods
    // ----------------------------------

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ENTOURAGE_CROP_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
    }

}
