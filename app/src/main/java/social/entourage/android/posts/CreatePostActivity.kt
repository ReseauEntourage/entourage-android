package social.entourage.android.posts

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.databinding.ActivityCreatePostBinding
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents
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

    // ID du "groupe" ou de l'"événement" (selon l’écran) récupéré via l’Intent
    protected var groupId = Const.DEFAULT_VALUE

    // Doit-on fermer l’Activity après création ?
    private var shouldClose = false

    // URI de l’image choisie (s’il y en a une)
    var imageURI: Uri? = null

    // --------------------------------------------------------------------
    // LOGIQUE DE MENTION
    // --------------------------------------------------------------------
    private var allMembers: List<EntourageUser> = emptyList()
    private var lastMentionStartIndex = -1

    // Adapter qui gère l’affichage des suggestions de mention
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user ->
            insertMentionIntoEditText(user)
        }
    }

    // --------------------------------------------------------------------
    // cycle de vie
    // --------------------------------------------------------------------
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

        // Configure le RecyclerView pour les suggestions @
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter

        // Active la détection du '@' dans le champ "message"
        setupMentionTextWatcher()
    }

    // --------------------------------------------------------------------
    // Méthode utilitaire pour les classes filles : si hasPost = true, on termine.
    // --------------------------------------------------------------------
    protected fun handlePost(hasPost: Boolean) {
        if (hasPost) {
            if (shouldClose) {
                var id = groupId
                if (id == -1 && CreatePostGroupActivity.idGroupForPost != null) {
                    id = CreatePostGroupActivity.idGroupForPost!!
                }
                // Petit délai avant de relancer éventuellement une autre activity
                Handler(Looper.getMainLooper()).postDelayed({
                    // Exemple : StartActivity(FeedActivity) si besoin
                    // startActivity(Intent(this, FeedActivity::class.java).putExtra(Const.GROUP_ID, id))
                    finish()
                }, 2000)
            }
            CreatePostGroupActivity.idGroupForPost = null
            finish()
        }
    }

    // --------------------------------------------------------------------
    // Récupération ou suppression d'une image
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
    // UI setup
    // --------------------------------------------------------------------
    private fun setView() {
        // On modifie simplement l’icône du bouton de validation
        binding.validate.button.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.new_post, null),
            null
        )
    }

    // --------------------------------------------------------------------
    // Récupération du résultat de la modal ChoosePhotoModalFragment
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

            // Cas 1 : pas d’image, uniquement du texte
            if (imageURI == null && isMessageValid()) {
                val messageChat = ArrayMap<String, Any>()

                // -------------------------------------
                // CORRECTION : utiliser Html.toHtml() pour conserver le HTML
                // -------------------------------------
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
            // Cas 2 : présence d’une image
            else if (imageURI != null) {
                imageURI?.let { uri ->
                    val file = Utils.getFile(this, uri)
                    addPostWithImage(file)
                }
            }
        }
    }

    /**
     * Méthodes abstraites à implémenter par les classes filles
     * pour poster AVEC ou SANS image.
     */
    abstract fun addPostWithImage(file: File)
    abstract fun addPostWithoutImage(request: ArrayMap<String, Any>)

    // --------------------------------------------------------------------
    // Bouton back
    // --------------------------------------------------------------------
    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    // --------------------------------------------------------------------
    // EditText "message": si on tape du texte, on active ou non le bouton
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
    // MISE À DISPOSITION D'UNE FONCTION POUR RECEVOIR LES MEMBRES
    // --------------------------------------------------------------------
    protected fun setAllMembers(members: List<EntourageUser>) {
        this.allMembers = members
    }

    // --------------------------------------------------------------------
    // LOGIQUE DE MENTION
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

                    if (mentionQuery.isEmpty()) {
                        showMentionSuggestions(allMembers)
                    } else {
                        filterAndShowMentions(mentionQuery)
                    }
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    private fun filterAndShowMentions(query: String) {
        val filtered = allMembers.filter {
            it.displayName?.contains(query, ignoreCase = true) == true
        }.take(5)
        showMentionSuggestions(filtered)
    }

    private fun showMentionSuggestions(members: List<EntourageUser>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
            return
        }
        binding.mentionSuggestionsContainer.visibility = View.VISIBLE
        mentionAdapter.updateList(members)
    }

    private fun hideMentionSuggestions() {
        binding.mentionSuggestionsContainer.visibility = View.GONE
    }

    private fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.message.selectionStart
        val editable = binding.message.editableText ?: return
        if (lastMentionStartIndex < 0) return

        val mentionHtml = """<a href="https://preprod.entourage.social/app/user/${user.userId}">@${user.displayName}</a>"""
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
