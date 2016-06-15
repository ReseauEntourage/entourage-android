package social.entourage.android.user.edit.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.github.clans.fab.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

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

    private Bitmap photo;

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

    public static PhotoEditFragment newInstance(Bitmap bitmap) {
        PhotoEditFragment fragment = new PhotoEditFragment();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Bundle args = new Bundle();
        args.putByteArray(PHOTO_PARAM, byteArray);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            byte[] byteArray = getArguments().getByteArray(PHOTO_PARAM);
            if (byteArray != null) {
                photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            }
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

        if (photo != null) {
            cropImageView.setImageBitmap(photo);
        }
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
        Bitmap cropped = cropImageView.getCroppedImage();
        if (mListener != null) {
            fabButton.setEnabled(false);
            mListener.onPhotoChosen(cropped);
        }
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    public void onPhotoSent(boolean success) {
        if (success && !fabButton.isEnabled()) {
            dismiss();
        } else {
            fabButton.setEnabled(true);
        }
    }

}
