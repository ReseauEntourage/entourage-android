package social.entourage.android.tools

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */
import android.util.Log
import social.entourage.android.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

fun Any.Logger(msg: String) {
    val dateFormat1 = SimpleDateFormat("hh:mm:ss")
    val stringDate = dateFormat1.format(Date())
    logger(tag, stringDate+" -> "+className + " : " + msg)
}

fun logger(tag:String,msg:String): Int? {
    return if (BuildConfig.DEBUG) Log.d(tag,msg) else null
}

private val Any.className: String
    get() = javaClass.simpleName
private val Any.tag: String
    get() = "ENTOURAGE_TEST"