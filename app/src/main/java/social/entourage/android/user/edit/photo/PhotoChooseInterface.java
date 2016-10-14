package social.entourage.android.user.edit.photo;


import android.net.Uri;

/**
 * Created by mihaiionescu on 14/06/16.
 */

public interface PhotoChooseInterface {

    void onPhotoIgnore();
    void onPhotoChosen(Uri photoURI);
}
