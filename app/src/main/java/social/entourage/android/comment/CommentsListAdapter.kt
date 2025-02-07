package social.entourage.android.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.databinding.*
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.user.UserProfileActivity
import timber.log.Timber
import java.text.SimpleDateFormat

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
    TYPE_DATE(2),
    TYPE_DETAIL(3)
}

interface OnItemClickListener {
    fun onItemClick(comment: Post)
    fun onCommentReport(commentId: Int?, isForEvent: Boolean, isMe: Boolean, commentLang: String)
    fun onShowWeb(url: String)
}

class CommentsListAdapter(
    var context: Context,
    private var commentsList: List<Post>,
    private var postAuthorId: Int,
    var isOne2One: Boolean,
    var isConversation: Boolean,
    private var currentParentPost: Post?,
    var onItemClick: OnItemClickListener,
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    var isForEvent: Boolean = false

    // Pour savoir si l'utilisateur veut la version traduite ou originale
    // On inverse si l'ID est dans translationExceptions
    private val translationExceptions = mutableSetOf<Int>()

    fun initiateList() {
        // Vérifie la config "translatedByDefault"
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getBoolean("translatedByDefault", true)

        // Marque tout post qui a "contentTranslations" comme potentiellement traduisible
        if (translatedByDefault) {
            commentsList.forEach {
                if (it.contentTranslations != null) {
                    translationExceptions.add(it.id!!)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setForEvent() {
        isForEvent = true
    }

    fun translateItem(commentId: Int) {
        // On inverse la logique pour cet ID
        if (translationExceptions.contains(commentId)) {
            translationExceptions.remove(commentId)
        } else {
            translationExceptions.add(commentId)
        }
        notifyItemChanged(commentsList.indexOfFirst { it.id == commentId } + 1)
    }

    inner class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindDate(comment: Post) {
            (binding as LayoutCommentItemDateBinding).publicationDay.text = comment.datePostText
        }

        // ----------------------------------------------------------------------------------------
        // bindLeft : Affiche un commentaire "à gauche"
        // ----------------------------------------------------------------------------------------
        fun bindLeft(comment: Post) {
            val bindingLeft = binding as LayoutCommentItemLeftBinding
            val isMe = (comment.user?.userId == EntourageApplication.get().me()?.id)

            // Détermine si on veut la version traduite ou originale
            val sharedPrefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
            val isTranslated = if (translationExceptions.contains(comment.id)) {
                !isTranslatedByDefault
            } else {
                isTranslatedByDefault
            }

            // Récupère la chaîne final
            var contentToShow = getFinalContent(comment, isTranslated)

            // Gère statuts "deleted"/"offensif"
            if (comment.status in listOf("deleted", "offensive", "offensible")) {
                handleDeletedOrOffensiveLeft(bindingLeft, comment, isMe)
                return
            }

            // On parse TOUT le temps en HTML + Linkify
            if (contentToShow.isNullOrEmpty()) contentToShow = ""
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(contentToShow, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(contentToShow)
            }
            bindingLeft.comment.text = spanned

            // Rendre cliquable (balises <a> + URL brutes)
            bindingLeft.comment.movementMethod = LinkMovementMethod.getInstance()
            Linkify.addLinks(bindingLeft.comment, Linkify.WEB_URLS)

            // Couleur de fond (orange si c'est moi, sinon beige)
            bindingLeft.comment.background = if (isMe) {
                context.getDrawable(R.drawable.new_comment_background_orange)
            } else {
                context.getDrawable(R.drawable.new_comment_background_beige)
            }
            bindingLeft.comment.setTextColor(context.getColor(R.color.black))

            // Bouton "report" + long clic => signale
            handleReportLogicLeft(bindingLeft, comment, isMe)

            // Date + gestion "error" si pas de date
            handleDateAndErrorLeft(bindingLeft, comment)

            // Gère l'auteur (photo, nom...)
            handleUserLeft(bindingLeft, comment, isMe)

            // Si isConversation => couleur orange pour userName + date
            if (isConversation) {
                bindingLeft.authorName.setTextColor(
                    context.getColor(R.color.light_orange)
                )
                bindingLeft.publicationDate.setTextColor(
                    context.getColor(R.color.light_orange)
                )
            }
        }

        // ----------------------------------------------------------------------------------------
        // bindRight : Affiche un commentaire "à droite"
        // ----------------------------------------------------------------------------------------
        fun bindRight(comment: Post) {
            val bindingRight = binding as LayoutCommentItemRightBinding
            val isMe = (comment.user?.userId == EntourageApplication.get().me()?.id)

            // Détermine si on veut la version traduite ou originale
            val sharedPrefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
            val isTranslated = if (translationExceptions.contains(comment.id)) {
                !isTranslatedByDefault
            } else {
                isTranslatedByDefault
            }

            // Récupère la chaîne final
            var contentToShow = getFinalContent(comment, isTranslated)
            Timber.wtf("contentToShow " + contentToShow)
            Timber.wtf("comment " + comment.toString())

            // Gère statuts "deleted"/"offensif"
            if (comment.status in listOf("deleted", "offensive", "offensible")) {
                handleDeletedOrOffensiveRight(bindingRight, comment, isMe)
                return
            }

            // On parse en HTML + Linkify
            if (contentToShow.isNullOrEmpty()) contentToShow = ""
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(contentToShow, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(contentToShow)
            }
            bindingRight.comment.text = spanned

            bindingRight.comment.movementMethod = LinkMovementMethod.getInstance()
            Linkify.addLinks(bindingRight.comment, Linkify.WEB_URLS)

            // Couleur de fond
            bindingRight.comment.background = if (isMe) {
                context.getDrawable(R.drawable.new_comment_background_orange)
            } else {
                context.getDrawable(R.drawable.new_comment_background_beige)
            }
            bindingRight.comment.setTextColor(context.getColor(R.color.black))

            // Bouton "report" + long clic => signale
            handleReportLogicRight(bindingRight, comment, isMe)

            // Date + gestion "error" si pas de date
            handleDateAndErrorRight(bindingRight, comment)

            // Gère l'auteur (photo, nom...)
            handleUserRight(bindingRight, comment, isMe)

            // Couleurs conversation
            if (isConversation) {
                bindingRight.authorName.setTextColor(
                    context.getColor(R.color.light_orange)
                )
                bindingRight.publicationDate.setTextColor(
                    context.getColor(R.color.light_orange)
                )
            }
        }

        // ----------------------------------------------------------------------------------------
        // bindDetails : affichage du "post parent" (TYPE_DETAIL)
        // ----------------------------------------------------------------------------------------
        fun bindDetails(comment: Post) {
            val bindingDetail = binding as LayoutCommentDetailPostTopBinding
            bindingDetail.commentPost.text = comment.content

            if (comment.status.equals("deleted")) {
                bindingDetail.commentPost.text = context.getString(R.string.deleted_publi)
            }
            bindingDetail.commentPost.setHyperlinkClickable()

            comment.createdTime?.let {
                val locale = LanguageManager.getLocaleFromPreferences(context)
                bindingDetail.publicationDatePost.text = "le " + SimpleDateFormat(
                    "dd.MM.yyyy",
                    locale
                ).format(it)
            }

            comment.imageUrl?.let { avatarURL ->
                bindingDetail.photoPost.visibility = View.VISIBLE
                Glide.with(bindingDetail.photoPost.context)
                    .load(avatarURL)
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .placeholder(R.drawable.new_group_illu)
                    .error(R.drawable.new_group_illu)
                    .into(bindingDetail.photoPost)
            } ?: run {
                bindingDetail.photoPost.visibility = View.GONE
            }

            bindingDetail.authorNamePost.text = comment.user?.displayName ?: "-"

            comment.user?.avatarURLAsString?.let { avatarURL ->
                Glide.with(bindingDetail.imagePost.context)
                    .load(avatarURL)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(bindingDetail.imagePost)
            } ?: run {
                Glide.with(bindingDetail.imagePost.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(bindingDetail.imagePost)
            }
        }

        // ----------------------------------------------------------------------------------------
        // Sous-fonctions pour statuts "deleted/offensive", report, date, user...
        // ----------------------------------------------------------------------------------------
        private fun handleDeletedOrOffensiveLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
            val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
            val width = 12
            val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
            val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledDrawable)
            vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
            vectorDrawable.draw(canvas)
            val grayDrawable = vectorDrawable.mutate()
            grayDrawable.setColorFilter(
                context.getColor(R.color.grey_deleted_icon),
                PorterDuff.Mode.SRC_IN
            )
            grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
            binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
            binding.comment.compoundDrawablePadding = 16

            if (isOne2One) {
                binding.comment.text = context.getString(R.string.deleted_message)
            } else {
                binding.comment.text = context.getString(R.string.deleted_comment)
            }
            if (comment.status in listOf("offensive", "offensible")) {
                binding.comment.text = context.getString(R.string.offensive_message)
            }
            binding.comment.background = context.getDrawable(R.drawable.new_comment_background_grey)
            binding.comment.setTextColor(context.getColor(R.color.grey_deleted_icon))
        }

        private fun handleDeletedOrOffensiveRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
            val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
            val width = 12
            val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
            val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledDrawable)
            vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
            vectorDrawable.draw(canvas)
            val grayDrawable = vectorDrawable.mutate()
            grayDrawable.setColorFilter(
                context.getColor(R.color.grey_deleted_icon),
                PorterDuff.Mode.SRC_IN
            )
            grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
            binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
            binding.comment.compoundDrawablePadding = 16

            if (isOne2One) {
                binding.comment.text = context.getString(R.string.deleted_message)
            } else {
                binding.comment.text = context.getString(R.string.deleted_comment)
            }
            if (comment.status in listOf("offensive", "offensible")) {
                binding.comment.text = context.getString(R.string.offensive_message)
            }
            binding.comment.background = context.getDrawable(R.drawable.new_comment_background_grey)
            binding.comment.setTextColor(context.getColor(R.color.grey_deleted_icon))
        }

        private fun handleReportLogicLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isMe, commentLang)
            }
            // Long click => report
            if (comment.status !in listOf("deleted", "offensive", "offensible")) {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isMe, commentLang)
                    true
                }
            }
            // Si c'est "moi" ou "isConversation", on masque "report"
            if (isMe || isConversation) {
                binding.report.visibility = View.GONE
            } else {
                binding.report.visibility = View.VISIBLE
            }
        }

        private fun handleReportLogicRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isMe, commentLang)
            }
            if (comment.status !in listOf("deleted", "offensive", "offensible")) {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isMe, commentLang)
                    true
                }
            }
            if (isMe || isConversation) {
                binding.report.visibility = View.GONE
            } else {
                binding.report.visibility = View.VISIBLE
            }
        }

        private fun handleDateAndErrorLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post
        ) {
            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                val locale = LanguageManager.getLocaleFromPreferences(context)
                if (isConversation) {
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm", locale).format(it)
                } else {
                    binding.publicationDate.text = "le " + SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)
                }
            } ?: run {
                // Pas de date => affiche "error" + onClick => resend
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }
        }

        private fun handleDateAndErrorRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post
        ) {
            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                val locale = LanguageManager.getLocaleFromPreferences(context)
                if (isConversation) {
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm", locale).format(it)
                } else {
                    binding.publicationDate.text = "le " + SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)
                }
            } ?: run {
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }
        }

        private fun handleUserLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            comment.user?.let { user ->
                binding.authorName.text = if (isMe) "" else user.displayName
                user.avatarURLAsString?.let { url ->
                    Glide.with(binding.root.context)
                        .load(url)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
                        .load(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                }
            }
            binding.image.setOnClickListener {
                comment.user?.userId?.let { userId ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = userId.toString()
                    (binding.image.context as? Activity)?.startActivityForResult(
                        Intent(binding.image.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID, userId
                        ), 0
                    )
                }
            }
        }

        private fun handleUserRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            comment.user?.let { user ->
                binding.authorName.text = if (isMe) "" else user.displayName
                user.avatarURLAsString?.let { url ->
                    Glide.with(binding.root.context)
                        .load(url)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
                        .load(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                }
            }
            binding.image.setOnClickListener {
                comment.user?.userId?.let { userId ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = userId.toString()
                    (binding.image.context as? Activity)?.startActivityForResult(
                        Intent(binding.image.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID, userId
                        ), 0
                    )
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Détermine s'il y a un "post parent"
    // --------------------------------------------------------------------------------------------
    fun updateData(currentParentPost: Post?) {
        this.currentParentPost = currentParentPost
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_DETAIL.code -> LayoutCommentDetailPostTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CommentsTypes.TYPE_LEFT.code -> LayoutCommentItemLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CommentsTypes.TYPE_RIGHT.code -> LayoutCommentItemRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> LayoutCommentItemDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = if (position == 0 && hasCurrentPost()) {
            currentParentPost
        } else {
            commentsList[ if (hasCurrentPost()) (position - 1) else position ]
        }
        if (comment == null) return

        when (getItemViewType(position)) {
            CommentsTypes.TYPE_DETAIL.code -> holder.bindDetails(comment)
            CommentsTypes.TYPE_LEFT.code -> holder.bindLeft(comment)
            CommentsTypes.TYPE_RIGHT.code -> holder.bindRight(comment)
            CommentsTypes.TYPE_DATE.code -> holder.bindDate(comment)
        }
    }

    private fun hasCurrentPost(): Boolean {
        return currentParentPost != null
    }

    override fun getItemCount(): Int {
        val addOne = if (hasCurrentPost()) 1 else 0
        return commentsList.size + addOne
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && hasCurrentPost()) {
            return CommentsTypes.TYPE_DETAIL.code
        }
        val realPos = if (hasCurrentPost()) position - 1 else position

        if (commentsList[realPos].isDatePostOnly) {
            return CommentsTypes.TYPE_DATE.code
        }
        // Qui a posté ? => on détermine gauche/droite
        return if (commentsList[realPos].user?.id?.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else {
            CommentsTypes.TYPE_LEFT.code
        }
    }

    // --------------------------------------------------------------------------------------------
    // Fonction utilitaire : renvoie la chaîne la plus adaptée (HTML vs normal, original vs traduction)
    // --------------------------------------------------------------------------------------------
    /**
     * Si isTranslated = false => on veut l'original
     *   => on tente d'abord contentHtml, sinon content
     * Si isTranslated = true => on veut la traduction
     *   => on tente d'abord contentTranslationsHtml?.translation, sinon contentTranslations?.translation
     */
    private fun getFinalContent(comment: Post, isTranslated: Boolean): String {
        Timber.d("getFinalContent => isTranslated=$isTranslated contentHtml='${comment.contentHtml}' content='${comment.content}'")

        if (!isTranslated) {
            if (!comment.contentHtml.isNullOrBlank()) {
                Timber.d("getFinalContent→ returning contentHtml")
                return comment.contentHtml
            } else if (!comment.content.isNullOrBlank()) {
                Timber.d("getFinalContent→ returning content")
                return comment.content
            } else {
                Timber.d("getFinalContent→ returning empty")
                return ""
            }
        } else {
            val htmlTranslation = comment.contentTranslationsHtml?.translation
            Timber.d("getFinalContent => htmlTranslation='$htmlTranslation'")
            if (!htmlTranslation.isNullOrBlank()) {
                Timber.d("getFinalContent→ returning contentTranslationsHtml.translation")
                return htmlTranslation
            } else {
                val normalTranslation = comment.contentTranslations?.translation
                Timber.d("getFinalContent => normalTranslation='$normalTranslation'")
                if (!normalTranslation.isNullOrBlank()) {
                    Timber.d("getFinalContent→ returning contentTranslations.translation")
                    return normalTranslation
                } else {
                    Timber.d("getFinalContent→ returning empty")
                    return comment.content ?: ""
                }
            }
        }
    }


}
