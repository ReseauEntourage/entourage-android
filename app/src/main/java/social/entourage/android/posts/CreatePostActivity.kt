package social.entourage.android.posts

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.databinding.ActivityCreatePostBinding
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.px
import java.io.File

/**
 * Activité abstraite gérant la création d'un Post (texte + image) et la logique de mentions.
 * Les classes filles (CreatePostEventActivity, CreatePostGroupActivity) y implémentent
 * la publication concrète via leur Presenter respectif.
 */
abstract class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreatePostBinding

    // ID du "groupe" ou "événement" récupéré via l’Intent
    protected var groupId = Const.DEFAULT_VALUE

    // Doit-on fermer l’Activity après la création ?
    private var shouldClose = false

    // URI de l’image choisie (s’il y en a une)
    var imageURI: Uri? = null

    // Détection mention: index du dernier '@' tapé
    private var lastMentionStartIndex = -1

    // Adapter pour afficher les suggestions de mentions
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user ->
            insertMentionIntoEditText(user)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_FEED_NEW_POST_SCREEN)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        shouldClose = intent.getBooleanExtra(Const.FROM_CREATE_GROUP, false)

        setView()
        handleDeleteImageButton()
        handleAddPhotoButton()
        getResult()
        validatePost()
        handleBackButton()
        handleMessageChangedTextListener()

        // Mise en place du RecyclerView pour les suggestions de mention
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter

        // Ecoute du '@' dans l'EditText
        setupMentionTextWatcher()

        updatePaddingTopForEdgeToEdge(binding.header.headerLayout)
    }

    /**
     * Méthode abstraite : appelée quand on a détecté un "@" + la suite du texte `query`.
     * Les classes filles (CreatePostGroupActivity / CreatePostEventActivity)
     * doivent appeler leur Presenter (searchGroupMembers / searchEventMembersRemote).
     */
    protected abstract fun onMentionQuery(query: String)

    /**
     * Méthode appelée par la classe fille pour mettre à jour la liste des suggestions
     * quand on reçoit le résultat asynchrone du Presenter.
     */
    fun updateMentionList(members: List<EntourageUser>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
        } else {
            showMentionSuggestions(members)
        }
    }

    // --------------------------------------------------------------------
    // Pour finaliser le post (hasPost=true => on ferme)
    // --------------------------------------------------------------------
    protected fun handlePost(hasPost: Boolean) {
        if (hasPost) {
            if (shouldClose) {
                var id = groupId
                if (id == -1 && CreatePostGroupActivity.idGroupForPost != null) {
                    id = CreatePostGroupActivity.idGroupForPost!!
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    // On pourrait éventuellement lancer une autre activité, etc.
                    finish()
                }, 2000)
            }
            CreatePostGroupActivity.idGroupForPost = null
            finish()
        }
    }

    // --------------------------------------------------------------------
    // Choix photo
    // --------------------------------------------------------------------
    private fun handleAddPhotoButton() {
        binding.addPhotoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_ADD_PIC)
            choosePhoto()
        }
        binding.photoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_ADD_PIC)
            choosePhoto()
        }
    }

    private fun choosePhoto() {
        val choosePhotoModalFragment =
            social.entourage.android.base.ChoosePhotoModalFragment.newInstance()
        choosePhotoModalFragment.show(
            supportFragmentManager,
            social.entourage.android.base.ChoosePhotoModalFragment.TAG
        )
    }

    private fun handleDeleteImageButton() {
        binding.deleteImage.setOnClickListener {
            binding.addPhotoLayout.visibility = View.VISIBLE
            binding.photoLayout.visibility = View.GONE
            imageURI = null
        }
    }

    // --------------------------------------------------------------------
    // UI
    // --------------------------------------------------------------------
    private fun setView() {
        binding.validate.button.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.new_post, null),
            null
        )
    }

    private fun forceMeasureViewHeight(view: View): Int {
        val originalVisibility = view.visibility
        if (originalVisibility == View.GONE) {
            view.visibility = View.INVISIBLE
        }
        val parentWidth = (view.parent as? View)?.width ?: 0
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        val measuredHeight = view.measuredHeight
        view.visibility = originalVisibility
        return measuredHeight
    }

    /**
     * Anime l'apparition/disparition du conteneur de suggestions de mentions depuis le haut.
     * Lors de l'apparition, le container démarre en translationY négative (hors écran) et glisse vers sa position.
     * Lors de la disparition, il repart vers le haut et disparaît.
     */
    private fun animateMentionSuggestionsFromTop(show: Boolean) {
        val container = binding.mentionSuggestionsContainer
        // Si la hauteur est nulle, on force la mesure
        val containerHeight = if (container.height > 0) {
            container.height.toFloat()
        } else {
            forceMeasureViewHeight(container).toFloat()
        }

        if (show) {
            container.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = -containerHeight
            }
            container.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            container.animate()
                .translationY(-containerHeight)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { container.visibility = View.GONE }
                .start()
        }
    }

    // --------------------------------------------------------------------
    // Résultat du ChoosePhotoModalFragment
    // --------------------------------------------------------------------
    private fun getResult() {
        supportFragmentManager.setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO, this) { _, bundle ->
            imageURI = bundle.getParcelable(Const.CHOOSE_PHOTO)
            imageURI?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.photoLayout.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
                handleSaveButtonState(true)
            }
        }
    }

    // --------------------------------------------------------------------
    // Publication du post
    // --------------------------------------------------------------------
    private fun validatePost() {
        binding.validate.button.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_VALIDATE)

            // Cas 1 : pas d'image, uniquement texte
            if (imageURI == null && isMessageValid()) {
                val messageChat = ArrayMap<String, Any>()
                val messageToSend = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.toHtml(binding.message.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                } else {
                    @Suppress("DEPRECATION")
                    Html.toHtml(binding.message.text)
                }
                messageChat["content"] = messageToSend
                val request = ArrayMap<String, Any>()
                request["chat_message"] = messageChat
                addPostWithoutImage(request)
            }
            // Cas 2 : il y a une image
            else if (imageURI != null) {
                imageURI?.let { uri ->
                    val file = Utils.getFile(this, uri)
                    addPostWithImage(file)
                }
            }
        }
    }

    abstract fun addPostWithImage(file: File)
    abstract fun addPostWithoutImage(request: ArrayMap<String, Any>)

    // --------------------------------------------------------------------
    // Bouton back
    // --------------------------------------------------------------------
    private fun handleBackButton() {
        binding.header.headerIconBack.setOnClickListener {
            finish()
        }
    }

    // --------------------------------------------------------------------
    // EditText
    // --------------------------------------------------------------------
    private fun handleMessageChangedTextListener() {
        binding.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                handleSaveButtonState(isMessageValid() || imageURI != null)
            }
        })
    }

    private fun isMessageValid(): Boolean {
        return binding.message.text.isNotEmpty() && binding.message.text.isNotBlank()
    }

    private fun handleSaveButtonState(isActive: Boolean) {
        val background = ContextCompat.getDrawable(
            this,
            if (isActive) R.drawable.new_rounded_button_orange
            else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.validate.button.background = background
    }

    // --------------------------------------------------------------------
    // LOGIQUE DE MENTION : on appelle onMentionQuery(query)
    // --------------------------------------------------------------------
    private fun setupMentionTextWatcher() {
        binding.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null) return
                val cursorPos = binding.message.selectionStart
                val substring = s.subSequence(0, cursorPos)
                val lastAt = substring.lastIndexOf('@')

                if (lastAt >= 0) {
                    val mentionQuery = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt
                    // On délègue la recherche mention à la classe fille
                    onMentionQuery(mentionQuery)
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    // On laisse le parent gérer l'affichage / masquage,
    // la mise à jour de mentionAdapter est déclenchée par updateMentionList(members).
    private fun showMentionSuggestions(members: List<EntourageUser>) {
        // Récupérer l'utilisateur courant
        val me = EntourageApplication.me(this)
        // Filtrer la liste pour retirer l'utilisateur courant
        val filteredMembers = members.filter { it.id != me?.id?.toLong() }

        mentionAdapter.updateList(filteredMembers)
        animateMentionSuggestionsFromTop(true)
    }
    private fun hideMentionSuggestions() {
        animateMentionSuggestionsFromTop(false)
    }

    private fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.message.selectionStart
        val editable = binding.message.editableText ?: return
        if (lastMentionStartIndex < 0) return

        var baseUrl = "https://" + BuildConfig.DEEP_LINKS_URL
        baseUrl = baseUrl.removeSuffix("/")

        // Nettoyer le displayName pour ne conserver que des lettres (Unicode inclus)
        val cleanedDisplayName = user.displayName?.replace(Regex("[^\\p{L}]"), "")  + ". "

        val mentionHtml = """<a href="$baseUrl/app/users/${user.userId}">@${cleanedDisplayName}</a>"""
        val mentionSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(mentionHtml, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(mentionHtml)
        }

        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)
        binding.message.setSelection(lastMentionStartIndex + mentionSpanned.length)
        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }

}
