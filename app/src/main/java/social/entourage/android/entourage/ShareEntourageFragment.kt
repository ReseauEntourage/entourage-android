package social.entourage.android.entourage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_share_entourage.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.EntourageMessageSharingAPI
import social.entourage.android.api.model.SharingEntourage
import social.entourage.android.base.EntourageDialogFragment

private const val ARG_PARAM1 = "uuid"

class ShareEntourageFragment : EntourageDialogFragment() {
    private var uuid = ""

    private var arraySharing:ArrayList<SharingEntourage> = ArrayList()
    private var adapter:ShareEntourageAdapter? = null

    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uuid = it.getString(ARG_PARAM1) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_share_entourage, container, false)
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

    fun sendSharing() {
        val sharing = arraySharing[selectedPosition]
        EntourageMessageSharingAPI.getInstance(EntourageApplication.get()).postSharingEntourage(sharing.uuid,uuid) { isOK ->

            Toast.makeText(activity, R.string.linkSahred, Toast.LENGTH_SHORT).show()

            dismiss()
        }
    }

    fun getSharing() {
        EntourageMessageSharingAPI.getInstance(EntourageApplication.get()).getSharing { isOk, sharing, error ->
            sharing?.let {
                arraySharing.clear()

                for (share in it.sharing) {
                    if (share.uuid != uuid) {
                        arraySharing.add(share)
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

        @JvmStatic
        fun newInstance(uuid: String?) =
                ShareEntourageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, uuid)
                    }
                }
    }
}