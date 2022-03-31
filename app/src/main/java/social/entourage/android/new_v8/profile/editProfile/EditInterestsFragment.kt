package social.entourage.android.new_v8.profile.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentEditInterestsBinding
import social.entourage.android.new_v8.profile.models.Interest
import timber.log.Timber

class EditInterestsFragment : Fragment() {


    private var _binding: NewFragmentEditInterestsBinding? = null
    val binding: NewFragmentEditInterestsBinding get() = _binding!!
    private lateinit var user: User
    private val editProfilePresenter: EditProfilePresenter by lazy { EditProfilePresenter() }

    private var interestsList: ArrayList<Interest> = ArrayList()
    private var selectedInterestIdList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEditInterestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        initializeInterests()
        setBackButton()
        setValidateButton()
    }


    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = InterestsListAdapter(interestsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Interest) {
                    item.id?.let { selectedInterestIdList.add(it) }
                }

                override fun onItemUncheck(item: Interest) {
                    selectedInterestIdList.remove(item.id)
                }
            })
        }
    }


    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setValidateButton() {
        binding.validate.button.setOnClickListener { onSaveInterests() }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val userInterests = user.interests
        tags?.interests?.forEach { interest ->
            interestsList.add(
                Interest(
                    interest.id,
                    interest.name,
                    userInterests.contains(interest.id)
                )
            )
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun onSaveInterests() {
        val editedUser: ArrayMap<String, Any> = ArrayMap()
        editedUser["interests"] = selectedInterestIdList
        editProfilePresenter.updateUser(editedUser)
    }
}