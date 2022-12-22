package social.entourage.android.old_v7.entourage.join

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import social.entourage.android.old_v7.MainActivity_v7
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.old_v7.user.UserFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.HtmlTextView

class EntourageJoinRequestReceivedActivity : BaseSecuredActivity() {
    private var message: Message? = null
    private var requestsCount = 0

    var presenter: EntourageJoinRequestReceivedPresenter =
        EntourageJoinRequestReceivedPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.v7_activity_entourage_join_request_received)
        message = intent.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?
        if (message != null) {
            displayMessage()
        }
    }

    private fun displayMessage() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_entourage_join_request_received_dialog, null)
        val htmlTextView: HtmlTextView = view.findViewById(R.id.entourage_join_request_received_text)
        var alertMessage = ""
        message?.content?.let {
            alertMessage = if (it.isEntourageRelated) {
                getString(R.string.entourage_join_request_received_message_html, message?.author)
            } else {
                getString(R.string.entourage_join_request_received_message_html, message?.author)
            }
        }
        htmlTextView.setHtmlString(alertMessage)
        htmlTextView.setOnClickListener { showUserProfile() }
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { _, _ ->
                    message?.content?.let {
                        requestsCount++
                        when {
                            it.isEntourageRelated -> {
                                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_ACCEPT)
                                presenter.acceptEntourageJoinRequest(it.joinableUUID, it.userId)
                            }
                            else -> {
                                finish()
                            }
                        }
                    }
                }
                .setNegativeButton(R.string.decline) { _, _ ->
                    message?.content?.let {
                        requestsCount++
                        when {
                            it.isEntourageRelated -> {
                                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_REJECT)
                                presenter.rejectEntourageJoinRequest(it.joinableUUID, it.userId)
                            }
                            else -> {
                                finish()
                            }
                        }
                    }
                }
                .setNeutralButton(R.string.user_view_profile) { dialog, which -> }
        if (requestsCount > 0) {
            builder.setNeutralButton(R.string.cancel) { dialog, which -> finish() }
        }
        val dialog = builder.create()
        dialog.show()
        if (requestsCount == 0) {
            //Overriding the view profile handler immediately after show so that it doesn't close the alert
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { showUserProfile() }
        }
    }

    fun onUserActionStatusChanged(status: String, statusChanged: Boolean) {
        if (isFinishing) return
        if (!statusChanged) {
            Toast.makeText(this, R.string.entourage_join_request_error, Toast.LENGTH_SHORT).show()
            displayMessage()
        } else {
            val messageId = if (FeedItem.JOIN_STATUS_REJECTED == status) R.string.entourage_join_request_rejected else R.string.entourage_join_request_success
            val toast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT)
            val duration = toast.duration
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(
                        this@EntourageJoinRequestReceivedActivity,
                        MainActivity_v7::class.java
                    )
                )
                finish()
            }, duration + 100.toLong())
        }
    }

    private fun showUserProfile() {
        val content = message?.content ?: return
        UserFragment.newInstance(content.userId).show(supportFragmentManager, UserFragment.TAG)
    }
}