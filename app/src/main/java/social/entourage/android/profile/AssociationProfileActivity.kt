package social.entourage.android.profile.association

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.FragmentAssociationProfileBinding
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import timber.log.Timber

class AssociationProfileActivity : BaseActivity() {

    private lateinit var binding: FragmentAssociationProfileBinding
    private val associationPresenter: AssociationPresenter by lazy { AssociationPresenter() }
    private var partner: Partner? = null
    private var partnerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAssociationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupération de l'ID via l'Intent au lieu de navArgs
        partnerId = intent.getIntExtra(Const.PARTNER_ID, -1)

        setBackButton()
        handleFollowButton()

        // Chargement des données
        associationPresenter.getPartnerInfos(partnerId)
        associationPresenter.getPartnerSuccess.observe(this, ::handleResponse)
        associationPresenter.followSuccess.observe(this, ::handleFollowResponse)
    }

    private fun handleFollowResponse(success: Boolean) {
        if (success) updateButtonFollow()
    }

    private fun handleResponse(success: Boolean) {
        if (success) {
            partner = associationPresenter.partner.value
            updateView()
        }
    }

    private fun updateView() {
        with(binding) {
            // --- Informations de base ---
            partner?.name?.let {
                assoProfileName.visibility = View.VISIBLE
                assoProfileName.text = it
            } ?: run { assoProfileName.visibility = View.GONE }

            partner?.description?.let {
                assoProfileDescription.visibility = View.VISIBLE
                assoProfileDescription.text = it
            } ?: run { assoProfileDescription.visibility = View.GONE }

            // --- Détails de contact (Layouts inclus) ---
            // On affiche le root du layout inclus seulement si la donnée existe
            assoProfilePhone.root.visibility = if (partner?.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
            assoProfilePhone.assoInfoContent.text = partner?.phone

            assoProfileWeb.root.visibility = if (partner?.websiteUrl.isNullOrEmpty()) View.GONE else View.VISIBLE
            assoProfileWeb.assoInfoContent.text = partner?.websiteUrl

            assoProfileAddress.root.visibility = if (partner?.address.isNullOrEmpty()) View.GONE else View.VISIBLE
            assoProfileAddress.assoInfoContent.text = partner?.address

            assoProfileEmail.root.visibility = if (partner?.email.isNullOrEmpty()) View.GONE else View.VISIBLE
            assoProfileEmail.assoInfoContent.text = partner?.email

            // --- Besoins de l'association ---
            val hasDonations = !partner?.donationsNeeds.isNullOrEmpty()
            val hasVolunteers = !partner?.volunteersNeeds.isNullOrEmpty()

            // Affiche le titre "Besoins" uniquement s'il y a au moins un type de besoin
            assoProfileNeeds.visibility = if (hasDonations || hasVolunteers) View.VISIBLE else View.GONE

            assoProfileDonation.root.visibility = if (hasDonations) View.VISIBLE else View.GONE
            assoProfileDonation.assoNeedsContent.text = partner?.donationsNeeds

            assoProfileVolunteers.root.visibility = if (hasVolunteers) View.VISIBLE else View.GONE
            assoProfileVolunteers.assoNeedsContent.text = partner?.volunteersNeeds

            val imgUrl = partner?.imageUrl

            if (!imgUrl.isNullOrEmpty()) {
                Glide.with(this@AssociationProfileActivity)
                    .load(imgUrl) // Glide accepte directement le String
                    .placeholder(R.drawable.partner_placeholder)
                    .error(R.drawable.partner_placeholder)
                    .circleCrop()
                    .into(assoProfileImageAssociation)
            } else {
                // Image par défaut si aucune URL n'est fournie
                assoProfileImageAssociation.setImageResource(R.drawable.partner_placeholder)
            }
        }
        // Met à jour l'état du bouton "Suivre"
        updateButtonFollow()
    }

    private fun updateButtonFollow() {
        partner?.let {
            val label = if (it.isFollowing) getString(R.string.following) else getString(R.string.follow)
            val textColor = ContextCompat.getColor(this, if (it.isFollowing) R.color.orange else R.color.white)
            val background = ResourcesCompat.getDrawable(resources, if (it.isFollowing) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill, null)
            val rightDrawable = ResourcesCompat.getDrawable(resources, if (it.isFollowing) R.drawable.new_check else R.drawable.new_plus_white, null)

            binding.assoProfileSubscribe.button.text = label
            binding.assoProfileSubscribe.button.setTextColor(textColor)
            binding.assoProfileSubscribe.button.background = background
            binding.assoProfileSubscribe.button.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
        }
    }

    private fun setBackButton() {
        binding.assoProfileIconBack.setOnClickListener {
            finish() // Ferme l'activité et revient à l'écran précédent
        }
    }

    private fun handleFollowButton() {
        binding.assoProfileSubscribe.button.setOnClickListener {
            partner?.let {
                if (it.isFollowing) {
                    CustomAlertDialog.showWithCancelFirst(this, getString(R.string.unsubscribe_title), getString(R.string.unsubscribe_content), getString(R.string.yes)) {
                        associationPresenter.updatePartnerFollow(!it.isFollowing, it.id)
                    }
                } else {
                    associationPresenter.updatePartnerFollow(!it.isFollowing, it.id)
                }
            }
        }
    }
}