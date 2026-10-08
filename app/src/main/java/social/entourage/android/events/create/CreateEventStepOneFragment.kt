package social.entourage.android.events.create

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.yalantis.ucrop.UCrop
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.FragmentCreateEventStepOneBinding
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.parcelableCompat
import social.entourage.android.tools.utils.px
import java.io.File

/** Étape 1 : nom, description et photo. */
class CreateEventStepOneFragment : Fragment() {

    private var _binding: FragmentCreateEventStepOneBinding? = null
    val binding: FragmentCreateEventStepOneBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                UCrop.getOutput(data)?.path?.let { path ->
                    viewModel.uploadLocalPhoto(File(path), requireContext().cacheDir)
                    renderPhoto()
                    Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR && data != null) {
                UCrop.getError(data)?.printStackTrace()
            }
        }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Nom de fichier unique : empêche Glide de resservir l'ancienne image depuis son cache mémoire.
                val uniqueFileName = "cropped_event_image_${System.currentTimeMillis()}.jpg"
                val destinationUri = Uri.fromFile(File(requireContext().cacheDir, uniqueFileName))

                val options = UCrop.Options()
                options.setToolbarTitle(getString(R.string.group_choose_photo))
                options.setCircleDimmedLayer(false)
                options.setHideBottomControls(true)
                options.setFreeStyleCropEnabled(false)

                val intent = UCrop.of(it, destinationUri)
                    .withAspectRatio(16f, 9f)
                    .withOptions(options)
                    .getIntent(requireContext())
                cropLauncher.launch(intent)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
        handleTexts()
        handleChoosePhoto()
        onFragmentResult()
        observeViewModel()
        if (!viewModel.isEdition) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_1)
        }
    }

    private fun setView() {
        val form = viewModel.form
        binding.eventName.setText(form.title)
        binding.eventDescription.setText(form.description)
        updateCounter(form.description.length)
        renderPhoto()
    }

    private fun handleTexts() {
        binding.eventName.doAfterTextChanged {
            viewModel.form.title = it?.toString() ?: ""
            viewModel.onFormChanged()
        }
        binding.eventDescription.doAfterTextChanged {
            viewModel.form.description = it?.toString() ?: ""
            updateCounter(viewModel.form.description.length)
            viewModel.onFormChanged()
        }
    }

    private fun updateCounter(length: Int) {
        binding.counter.text = String.format(
            getString(R.string.events_description_counter),
            length.toString()
        )
    }

    private fun handleChoosePhoto() {
        val openChooser = View.OnClickListener {
            ChooseGalleryPhotoModalFragment.newInstance(ImagesType.EVENTS)
                .show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
        }
        binding.addPhotoLayout.setOnClickListener(openChooser)
        binding.addPhoto.setOnClickListener(openChooser)
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            val isAddPhoto = bundle.getBoolean("is_add_photo", false)
            if (isAddPhoto) {
                getContent.launch("image/*")
            } else {
                bundle.parcelableCompat<Image>(Const.CHOOSE_PHOTO_PATH)?.let {
                    viewModel.selectCatalogImage(it)
                    renderPhoto()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.errors.observe(viewLifecycleOwner) { errors ->
            val name = errors[CreateEventField.NAME]
            val description = errors[CreateEventField.DESCRIPTION]
            val photo = errors[CreateEventField.PHOTO]
            binding.eventNameError.bindError(name)
            binding.eventName.bindErrorState(name != null)
            binding.eventDescriptionError.bindError(description)
            binding.eventDescription.bindErrorState(description != null)
            binding.photoError.bindError(photo)
            binding.addPhotoLayout.bindErrorState(photo != null)
        }
        viewModel.uploadFailed.observe(viewLifecycleOwner) { failed ->
            if (failed) {
                Utils.showToast(requireContext(), getString(R.string.create_event_photo_upload_error))
                viewModel.consumeUploadFailed()
                renderPhoto()
            }
        }
    }

    /** Affiche la photo choisie (copie locale, image du catalogue ou de l'événement édité). */
    private fun renderPhoto() {
        val form = viewModel.form
        val source: Any? = form.localPhotoPath?.let { File(it) } ?: form.photoUrl?.let { Uri.parse(it) }
        if (source != null) {
            binding.addPhotoLayout.visibility = View.GONE
            binding.addPhoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(source)
                .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                .into(binding.addPhoto)
        } else {
            binding.addPhoto.visibility = View.GONE
            binding.addPhotoLayout.visibility = View.VISIBLE
            // Photo déjà envoyée mais sans aperçu disponible (brouillon restauré) : on le dit.
            val alreadyChosen = form.hasPhoto()
            binding.addPhotoTitle.setText(
                if (alreadyChosen) R.string.create_event_photo_added else R.string.create_event_photo_add
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
