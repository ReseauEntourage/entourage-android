package social.entourage.android.tools.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_share_message.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MessageSharingAPI
import social.entourage.android.api.model.SharingEntourage
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.entourage.ShareEntourageAdapter

class ShareMessageFragment : BaseDialogFragment() {
    private var uuid = ""
    private var isPoi = false

    private var arraySharing:ArrayList<SharingEntourage> = ArrayList()
    private var adapter: ShareEntourageAdapter? = null

    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uuid = it.getString(ARG_UUID) ?: ""
            isPoi = it.getBoolean(ARG_ISPOI)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_share_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_button_validate_share_entourage?.visibility = View.GONE

        title_close_button?.setOnClickListener {
            dismiss()
        }

        ui_button_validate_share_entourage?.setOnClickListener {
            sendSharing()
        }

        setupViews()

        getSharing()
    }

    private fun sendSharing() {
        val sharing = arraySharing[selectedPosition]
        MessageSharingAPI.getInstance(EntourageApplication.get()).postSharingMessage(sharing.uuid,uuid,isPoi) { isOK ->
            activity?.let {
                Toast.makeText(it, if(isOK) R.string.linkShared else R.string.linkNotShared, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    fun getSharing() {
        MessageSharingAPI.getInstance(EntourageApplication.get()).getSharing { _, sharing, _ ->
            sharing?.let {
                arraySharing.clear()

                for (share in it.sharing) {
                    if (isPoi) {
                        arraySharing.add(share)
                    }
                    else {
                        if (share.uuid != uuid) {
                            arraySharing.add(share)
                        }
                    }
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    fun setupViews() {
        adapter = ShareEntourageAdapter(requireContext(),arraySharing) { position ->
            for (i in arraySharing.indices ) {
                if (i == position) {
                    arraySharing[i].isSelected = !arraySharing[i].isSelected
                }
                else {
                    arraySharing[i].isSelected = false
                }
            }
            if (arraySharing[position].isSelected) {
                ui_button_validate_share_entourage.visibility = View.VISIBLE
            }
            else {
                ui_button_validate_share_entourage.visibility = View.GONE
            }
            selectedPosition = position
            adapter?.notifyDataSetChanged()
        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        ui_recyclerView_share_entourage?.setHasFixedSize(true)
        ui_recyclerView_share_entourage?.layoutManager = linearLayoutManager
        ui_recyclerView_share_entourage?.adapter = adapter
    }

    companion object {
        const val TAG = "social.entourage.android.entourage.shareEntourageFragment"
        const val ARG_UUID = "uuid"
        const val ARG_ISPOI = "isPoi"

        fun newInstance(uuid: String) =
                ShareMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_UUID, uuid)
                        putBoolean(ARG_ISPOI,false)
                    }
                }

        fun newInstanceForPoi(poiId: String) =
                ShareMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_UUID,poiId)
                        putBoolean(ARG_ISPOI,true)
                    }
                }
    }
}