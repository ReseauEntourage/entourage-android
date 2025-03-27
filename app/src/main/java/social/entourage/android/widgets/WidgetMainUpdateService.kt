package social.entourage.android.widgets

import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import social.entourage.android.R
import social.entourage.android.events.EventsPresenter

class WidgetMainUpdateService : IntentService("WidgetMainUpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: return

        val views = RemoteViews(packageName, R.layout.widget_main)
        val presenter = EventsPresenter()

        Handler(Looper.getMainLooper()).post {
            presenter.getAllEvents.observeForever { eventList ->
                val count = eventList?.size ?: 0
                views.setTextViewText(R.id.eventCountText, "$count évènements à venir")

                val deeplinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://preprod.entourage.social/app/outings"))
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    deeplinkIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                val manager = AppWidgetManager.getInstance(this)
                manager.updateAppWidget(widgetId, views)

                // ⚠️ Si tu veux nettoyer l'observer : stocke-le dans une variable
                // et appelle removeObserver(observer) plus tard
            }

            presenter.getAllEvents(
                page = 1,
                per = 20,
                distance = null,
                latitude = null,
                longitude = null,
                period = "future"
            )
        }
    }
}
