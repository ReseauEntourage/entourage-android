package social.entourage.android.api.tape

import android.content.Context
import android.content.Intent
import com.squareup.tape.ObjectQueue
import com.squareup.tape.TaskQueue
import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask

class EncounterTapeTaskQueue(delegate: ObjectQueue<EncounterUploadTask?>?, private val context: Context) : TaskQueue<EncounterUploadTask?>(delegate) {
    fun start(): Boolean {
        if (size() > 0) {
            context.startService(Intent(context, EncounterTapeService::class.java))
            return true
        }
        return false
    }

    override fun add(entry: EncounterUploadTask?) {
        super.add(entry)
        start()
    }

}