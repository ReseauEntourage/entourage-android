package social.entourage.android.discussions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import social.entourage.android.R
import social.entourage.android.comment.CommentActivity
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import java.util.*

/**
 * Created by - on 15/11/2022.
 */
class DetailConversationActivity : CommentActivity() {

    private var hasToShowFirstMessage = false
    var hasSeveralpeople = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)

        viewModel.getAllComments.observe(this, ::handleGetPostComments)
        viewModel.commentPosted.observe(this, ::handleCommentPosted)

        viewModel.getPostComments(id)

        binding.header.iconSettings.setImageDrawable(resources.getDrawable(R.drawable.new_settings))
        binding.header.cardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.title = titleName

        if (isOne2One) {
            binding.header.headerTitle.setOnClickListener {
                ProfileFullActivity.isMe = false
                ProfileFullActivity.userId = postAuthorID.toString()
                startActivityForResult(
                    Intent(this, ProfileFullActivity::class.java).putExtra(
                        Const.USER_ID, postAuthorID
                    ),
                    0
                )
            }
        }

        viewModel.detailConversation.observe(this, ::handleDetailConversation)
        viewModel.getDetailConversation(id)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    override fun translateView(id: Int) {
        val adapter = binding.comments.adapter as? CommentsListAdapter
        adapter?.translateItem(id)
    }

    private fun handleDetailConversation(conversation: Conversation?) {
        // Parcourt la liste des messages de manière sécurisée
        conversation?.message?.forEach { message ->
            message.userRole?.let { role ->
                if (role.contains("Équipe Entourage")) {
                    hasToShowFirstMessage = false
                }
            }
        }

        checkAndShowPopWarning()

        // Met à jour le titre en vérifiant si conversation?.title n'est pas null
        titleName = conversation?.title ?: titleName
        binding.header.title = titleName

        val members = conversation?.members
        val memberCount = members?.size ?: 0

        // Si besoin, traiter chaque membre (boucle actuellement vide)
        members?.forEach { member ->
            // Traitement éventuel pour chaque membre
        }

        if (memberCount > 2) {
            this.hasSeveralpeople = true
            val displayName = conversation?.user?.displayName ?: ""
            // On utilise conversation?.memberCount si disponible, sinon on se base sur memberCount
            val convMemberCount = conversation?.memberCount ?: memberCount
            binding.header.title = "$displayName + ${convMemberCount - 1} membres"
        }

        if (conversation?.hasBlocker() == true) {
            // Ici, conversation est non-null (sinon la condition ne serait pas vraie)
            conversation.let { conv ->
                binding.postBlocked.isVisible = true
                val _name = titleName ?: ""
                binding.commentBlocked.hint = if (conv.imBlocker()) {
                    String.format(getString(R.string.message_user_blocked_by_me), _name)
                } else {
                    String.format(getString(R.string.message_user_blocked_by_other), _name)
                }
            }
        } else {
            binding.postBlocked.isVisible = false
        }
    }

    fun checkAndShowPopWarning() {
        if (hasToShowFirstMessage) {
            // Affiche le layout d'information si nécessaire
            binding.layoutInfoNewDiscussion.isVisible = true
            binding.uiIvCloseNew.setOnClickListener {
                binding.layoutInfoNewDiscussion.visibility = View.GONE
            }
        }
    }

    override fun addComment() {
        viewModel.addComment(id, comment)
    }

    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        val newComments = sortAndExtractDays(allComments, this)
        commentsList.clear()
        newComments?.let { commentsList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        newComments?.isEmpty()?.let { updateView(it) }
        scrollAfterLayout()
    }

    override fun handleReportPost(id: Int, commentLang: String) {
        binding.header.iconSettings.setOnClickListener {
            DataLanguageStock.updatePostLanguage(commentLang)
            AnalyticsEvents.logEvent(AnalyticsEvents.Message_action_param)
            SettingsDiscussionModalFragment.isSeveralPersonneInConversation = this.hasSeveralpeople
            SettingsDiscussionModalFragment.newInstance(
                postAuthorID,
                id,
                isOne2One,
                titleName,
                viewModel.detailConversation.value?.imBlocker()
            ).show(supportFragmentManager, SettingsDiscussionModalFragment.TAG)
        }
    }

    fun sortAndExtractDays(allEvents: MutableList<Post>?, context: Context): MutableList<Post>? {
        // Charge la langue préférée de l'utilisateur
        val languageCode = LanguageManager.loadLanguageFromPreferences(context)
        val locale = Locale(languageCode)
        val groupedEvents = allEvents?.groupBy { it.getFormatedStr() }
        val newList = ArrayList<Post>()
        groupedEvents?.forEach { (formattedStr, posts) ->
            val datePost = Post().apply {
                isDatePostOnly = true
                // Utilise la locale chargée pour formater la date
                datePostText = formattedStr.capitalize(locale)
            }
            newList.add(datePost)
            newList.addAll(posts)
        }
        return newList
    }

    fun updateDiscussion() {
        viewModel.getDetailConversation(id)
    }
}
