package social.entourage.android.user.edit.photo

import android.net.Uri

//**********//**********//**********
// Interface
//**********//**********//**********
interface PhotoEditInterface {
    fun onPhotoEdited(photoURI: Uri?, photoSource: Int)
}