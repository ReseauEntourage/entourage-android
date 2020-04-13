package social.entourage.android.user.edit.photo;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import timber.log.Timber;

public class PhotoEditFragment extends EntourageDialogFragment implements CropImageView.OnSetImageUriCompleteListener{

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.photo_edit";

    private static final String PHOTO_PARAM = "social.entourage.android.photo_param";
    private static final String PHOTO_SOURCE = "social.entourage.android.photo_source";

    private static final int ROTATE_DEGREES_STEP = -90;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @BindView(R.id.photo_edit_cropImageView)
    CropImageView cropImageView;
    @BindView(R.id.photo_edit_fab_button)
    FloatingActionButton fabButton;
    @BindView(R.id.photo_edit_progressBar)
    ProgressBar progressBar;

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
    public void onAttach(@NonNull Context context) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_9);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_edit, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() != null) {
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        cropImageView.setOnSetImageUriCompleteListener(this);
        if (photoUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            cropImageView.setImageUriAsync(photoUri);
        }
        //TODO use new lib com.theartofdev.edmodo:android-image-cropper 2.8 when migrating to AndroidX but meanwhile use this trick to avoid a crash
        cropImageView.setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL);
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

    @OnClick({R.id.photo_edit_rotate_button})
    protected void onRotateClicked() {
        cropImageView.rotateImage(ROTATE_DEGREES_STEP);
        EntourageEvents.logEvent(EntourageEvents.EVENT_USER_ROTATE_PHOTO);
    }

    // ----------------------------------
    // Upload handling
    // ----------------------------------

    @OnClick(R.id.photo_edit_fab_button)
    protected void onOkClicked() {
        fabButton.setEnabled(false);
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(final CropImageView view, final CropImageView.CropResult result) {
                if (result.isSuccessful()) {
                    mListener.onPhotoChosen(result.getUri(), photoSource);
                } else {
                    Timber.e(result.getError());
                    Toast.makeText(getActivity(), R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show();
                    fabButton.setEnabled(true);
                }
            }
        });
        try {
            File croppedImageFile = createImageFile();
            cropImageView.saveCroppedImageAsync(Uri.fromFile(croppedImageFile));
        } catch (IOException e) {
            Toast.makeText(getActivity(), R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // CropImageView.OnSetImageUriCompleteListener
    // ----------------------------------

    @Override
    public void onSetImageUriComplete(final CropImageView view, final Uri uri, final Exception error) {
        progressBar.setVisibility(View.GONE);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
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
