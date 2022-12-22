package social.entourage.android.old_v7.entourage.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.v7_fragment_create_entourage_photo_gallery.*
import kotlinx.android.synthetic.main.v7_fragment_create_entourage_photo_gallery.ui_recyclerview
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.api.EventPhotoGalleryApi
import social.entourage.android.api.PhotoGallery
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable

private const val ARG_PORTRAITURL = "portrait"
private const val ARG_LANDSCAPEURL = "landscape"

class CreateEntouragePhotoGalleryFragment : BaseDialogFragment() {
    private var portrait_url: String? = null
    private var landscape_url: String? = null

    private var mListener: CreateEntourageListener? = null

    var adapter: EntouragePhotoGalleryAdapter? = null
    var arrayPhotos = ArrayList<PhotoGallery>()
    var selectedItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            portrait_url = it.getString(ARG_PORTRAITURL)
            landscape_url = it.getString(ARG_LANDSCAPEURL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.v7_fragment_create_entourage_photo_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title_close_button?.setOnClickListener { onCloseClicked() }
        title_action_button?.setOnClickListener { onValidateClicked() }

        setupRecyclerView()

        getPhotoGallery()

        updateButtonValidate()
    }

    fun setupRecyclerView() {
        ui_tv_desription
        ui_recyclerview

        val listener = object : PhotoGalleryListener {
            override fun onPhotoSelected(position: Int) {
                selectedItem = if (selectedItem == position) -1 else position
                adapter?.updateSelection(selectedItem)
                updateButtonValidate()
            }
        }

        adapter = EntouragePhotoGalleryAdapter(arrayPhotos, listener)
        ui_recyclerview?.layoutManager = LinearLayoutManager(context)
        ui_recyclerview?.adapter = adapter
    }

    fun updateButtonValidate() {
        if (selectedItem == -1) {
            title_action_button?.disable()
        }
        else {
            title_action_button?.enable()
        }
    }

    fun getPhotoGallery() {
        EventPhotoGalleryApi.getInstance().getPhotoGallery { photoGallery, error ->
            photoGallery?.let {
                arrayPhotos = ArrayList()
                arrayPhotos.addAll(it)
                selectedItem = -1

                for (i in 0 until arrayPhotos.size) {
                    if (arrayPhotos[i].url_image_landscape.equals(landscape_url,true)) {
                        selectedItem = i
                        break
                    }
                }
                adapter?.updateDatas(arrayPhotos,selectedItem)
            }
        }
    }

    fun setListener(mListener: CreateEntourageListener?) {
        this.mListener = mListener
    }

    fun onCloseClicked() {
        mListener = null
        dismiss()
    }

    fun onValidateClicked() {
        if (selectedItem == -1) return
        mListener?.onPhotoEventAdded(arrayPhotos[selectedItem].url_image_portrait,arrayPhotos[selectedItem].url_image_landscape)
        mListener = null
        dismiss()
    }

    companion object {
        val TAG: String? = CreateEntouragePhotoGalleryFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(portrait_url: String?, landscape_url: String?) =
                CreateEntouragePhotoGalleryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PORTRAITURL, portrait_url)
                        putString(ARG_LANDSCAPEURL, landscape_url)
                    }
                }
    }
}