package social.entourage.android.new_v8.events.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentCreateEventStepFourBinding
import social.entourage.android.new_v8.models.Interest
import social.entourage.android.new_v8.profile.editProfile.InterestsListAdapter
import social.entourage.android.new_v8.profile.editProfile.OnItemCheckListener
import timber.log.Timber

class CreateEventStepFourFragment : Fragment() {

    private var _binding: NewFragmentCreateEventStepFourBinding? = null
    val binding: NewFragmentCreateEventStepFourBinding get() = _binding!!

    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()

    private lateinit var interestsListAdapter: InterestsListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        initializeInterests()
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        tags?.interests?.forEach { interest ->
            interestsList.add(
                Interest(
                    interest.id,
                    interest.name,
                    false
                )
            )
        }
        binding.layout.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initializeInterests() {
        interestsListAdapter = InterestsListAdapter(interestsList, object : OnItemCheckListener {
            override fun onItemCheck(item: Interest) {
                item.id?.let {
                    selectedInterestIdList.add(it)
                    CommunicationHandler.isButtonClickable.value = interestHaveBeenSelected()
                }
            }

            override fun onItemUncheck(item: Interest) {
                selectedInterestIdList.remove(item.id)
                CommunicationHandler.isButtonClickable.value = interestHaveBeenSelected()
            }
        }, true)
        binding.layout.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = interestsListAdapter
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            when {
                selectedInterestIdList.isEmpty() -> {
                    binding.layout.error.root.visibility = View.VISIBLE
                    binding.layout.error.errorMessage.text =
                        getString(R.string.error_categories_create_group)
                    CommunicationHandler.isCondition.value = false
                }
                else -> {
                    binding.layout.error.root.visibility = View.GONE
                    CommunicationHandler.isCondition.value = true
                    CommunicationHandler.event.interests(selectedInterestIdList)
                    interestsListAdapter.getOtherInterestCategory()
                        ?.let { if (it.isNotEmpty()) CommunicationHandler.event.otherInterest(it) }
                    CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
                    Timber.e("Event ${CommunicationHandler.event}")

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value = interestHaveBeenSelected()
    }


    fun interestHaveBeenSelected(): Boolean {
        return selectedInterestIdList.isNotEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }

}