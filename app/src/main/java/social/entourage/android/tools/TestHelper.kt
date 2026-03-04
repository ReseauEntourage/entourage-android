package social.entourage.android.tools

object TestHelper {
    private var isTesting: Boolean? = null

    var forceHarness = true
        set(value) {
            field = value
            //force to check class next time we need forceHarness
            isTesting = null
        }

    fun isRunningInTestHarness(): Boolean {
        if (isTesting == null) {
            isTesting = try {
                Class.forName("androidx.test.espresso.Espresso")
                forceHarness
            } catch (e: ClassNotFoundException) {
                false
            }
        }
        return isTesting!!
    }
}
