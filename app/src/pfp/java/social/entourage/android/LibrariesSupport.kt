package social.entourage.android

/**
 * Libraries support class specific to Voisinage
 * Created by Mihai Ionescu on 27/04/2018.
 */
class LibrariesSupport : BaseLibrariesSupport(){

    override fun setupLibraries(context: Context) {
        super.setupLibraries(context)
        setupTimberTree()
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------
    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }
}
