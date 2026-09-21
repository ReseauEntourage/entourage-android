package social.entourage.android.e2e

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import timber.log.Timber
import java.io.File

/**
 * Prend un screenshot du device à chaque étape d'un scénario E2E.
 * Les fichiers sont écrits sur le device sous /sdcard/Download/test_screenshot/<scenario>/
 * puis récupérés sur la machine hôte (dossier test_screenshot/ à la racine du projet)
 * via la tâche gradle `pullE2EScreenshots`.
 */
class E2EScreenshot(private val scenario: String) {
    private var counter = 0

    fun shoot(label: String) {
        counter++
        val safeLabel = label.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val fileName = "%02d_%s.png".format(counter, safeLabel)
        val storageDir = File("/sdcard/Download/test_screenshot/$scenario")

        try {
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val file = File(storageDir, fileName)
            val instrumentation = InstrumentationRegistry.getInstrumentation()

            if (UiDevice.getInstance(instrumentation).takeScreenshot(file)) {
                Timber.tag("E2E").i("Screenshot: $scenario/$fileName")
            } else {
                Timber.tag("E2E").e("Failed to take screenshot: $scenario/$fileName")
            }
        } catch (e: Exception) {
            Timber.tag("E2E").e(e, "Error taking screenshot: $scenario/$fileName")
        }
    }
}
