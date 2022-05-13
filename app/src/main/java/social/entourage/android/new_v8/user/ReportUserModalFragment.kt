package social.entourage.android.new_v8.user

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.MetaData
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentReportUserBinding
import social.entourage.android.new_v8.utils.Const


class ReportUserModalFragment : BottomSheetDialogFragment() {

    private var signalList: MutableList<MetaData> = ArrayList()
    private var _binding: NewFragmentReportUserBinding? = null
    val binding: NewFragmentReportUserBinding get() = _binding!!
    private var selectedSignalsIdList: MutableList<String> = mutableListOf()
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private var userReportedId: Int? = Const.DEFAULT_VALUE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentReportUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInterests()
        getUserReportedId()
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        userPresenter.isUserReported.observe(requireActivity(), ::handleReportResponse)
        setupViewStep1()
        handleCloseButton()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onClose()
    }

    private fun handleReportResponse(success: Boolean) {
        showToast(
            if (success) getString(R.string.user_report_success)
            else getString(R.string.user_report_error_send_failed)
        )
        dismiss()
    }

    private fun handleMetaData(tags: Tags?) {
        signalList.clear()
        tags?.signals?.let { signalList.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getUserReportedId() {
        userReportedId = arguments?.getInt(Const.USER_REPORTED_ID)
    }

    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReportUserListAdapter(signalList, object : OnItemCheckListener {
                override fun onItemCheck(item: MetaData) {
                    item.id?.let { selectedSignalsIdList.add(it) }
                }

                override fun onItemUncheck(item: MetaData) {
                    selectedSignalsIdList.remove(item.id)
                }
            })
        }
    }

    private fun setupViewStep1() {
        binding.mandatory.text = getString(R.string.mandatory)
        binding.divider.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.next.visibility = View.VISIBLE
        binding.next.setOnClickListener {
            if (selectedSignalsIdList.isNotEmpty())
                setupViewStep2()
        }
        binding.message.visibility = View.GONE
        binding.back.visibility = View.GONE
        binding.send.visibility = View.GONE
    }

    private fun setupViewStep2() {
        binding.mandatory.text = getString(R.string.optional)
        binding.message.visibility = View.VISIBLE
        binding.back.visibility = View.VISIBLE
        binding.send.visibility = View.VISIBLE
        binding.back.setOnClickListener {
            setupViewStep1()
        }
        binding.send.setOnClickListener {
            userReportedId?.let { id ->
                userPresenter.sendReport(
                    id,
                    binding.message.text.toString(),
                    selectedSignalsIdList
                )
            }
        }
        binding.divider.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.next.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun onClose() {
        selectedSignalsIdList.clear()
        userPresenter.isUserReported = MutableLiveData()
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            onClose()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ReportUserModalFragment"
        fun newInstance(id: Int): ReportUserModalFragment {
            val fragment = ReportUserModalFragment()
            val args = Bundle()
            args.putInt(Const.USER_REPORTED_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}