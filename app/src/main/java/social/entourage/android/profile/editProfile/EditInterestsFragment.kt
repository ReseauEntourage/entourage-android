package social.entourage.android.profile.editProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Interest
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentEditInterestsBinding

class EditInterestsFragment : Fragment() {

    private var _binding: NewFragmentEditInterestsBinding? = null
    val binding: NewFragmentEditInterestsBinding get() = _binding!!
    private lateinit var user: User
    private val editProfilePresenter: EditProfilePresenter by lazy { EditProfilePresenter() }

    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()

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
        editProfilePresenter.isUserUpdated.observe(requireActivity(), ::handleUpdateResponse)

    }

    private fun handleUpdateResponse(success: Boolean) {
        if (!isAdded) return // Vérifie si le fragment est attaché
        if (success) findNavController().popBackStack()
        else Toast.makeText(
            requireActivity(),
            R.string.user_text_update_ko,
            Toast.LENGTH_SHORT
        ).show()
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
            }, false)
        }
    }

    private fun setBackButton() {
        binding.header.headerIconBack.setOnClickListener {
            findNavController().popBackStack()
        }
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
            if (userInterests.contains(interest.id)) interest.id?.let {
                selectedInterestIdList.add(it)
            }
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun onSaveInterests() {
        val editedUser: MutableMap<String, Any> = mutableMapOf()
        val user: ArrayMap<String, Any> = ArrayMap()
        editedUser["interests"] = selectedInterestIdList
        user["user"] = editedUser
        editProfilePresenter.updateUser(user)
    }
}