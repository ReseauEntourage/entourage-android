package social.entourage.android.tools

object TestHelper {
    private var isTesting: Boolean? = null

    var forceHarness = true

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
