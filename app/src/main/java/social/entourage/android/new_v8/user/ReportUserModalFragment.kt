package social.entourage.android.new_v8.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags

class ReportUserModalFragment : BottomSheetDialogFragment() {

    private var signalList: MutableList<String> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.new_fragment_report_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)

    }

    private fun handleMetaData(tags: Tags?) {
        signalList.clear()
        tags?.interests?.forEach { interest ->
            interest.name?.let { it -> signalList.add(it) }
        }
    }

    companion object {
        const val TAG = "ReportUserModalFragment"
    }
}