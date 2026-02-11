package social.entourage.android.tools.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MessageSharingAPI
import social.entourage.android.api.model.SharingEntourage
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.FragmentShareMessageBinding
import social.entourage.android.entourage.ShareEntourageAdapter

class ShareMessageFragment : BaseDialogFragment() {
    private var _binding: FragmentShareMessageBinding? = null
    val binding: FragmentShareMessageBinding get() = _binding!!

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

        _binding = FragmentShareMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uiButtonValidateShareEntourage?.visibility = View.GONE

        binding.inviteContactsNavigationLayout.binding.titleCloseButton.setOnClickListener {
            dismiss()
        }

        binding.uiButtonValidateShareEntourage?.setOnClickListener {
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
                binding.uiButtonValidateShareEntourage.visibility = View.VISIBLE
            }
            else {
                binding.uiButtonValidateShareEntourage.visibility = View.GONE
            }
            selectedPosition = position
            adapter?.notifyDataSetChanged()
        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.uiRecyclerViewShareEntourage?.setHasFixedSize(true)
        binding.uiRecyclerViewShareEntourage?.layoutManager = linearLayoutManager
        binding.uiRecyclerViewShareEntourage?.adapter = adapter
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