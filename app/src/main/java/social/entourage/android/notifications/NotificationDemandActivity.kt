package social.entourage.android.notifications

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityNotificationDemandBinding
import social.entourage.android.tools.log.AnalyticsEvents

class NotificationDemandActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationDemandBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationSettingsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDemandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AnalyticsEvents.logEvent(AnalyticsEvents.notification_settings_page_view)

        // Initialize the permission launcher
        initializePermissionLauncher()

        // Initialize the notification settings launcher
        initializeNotificationSettingsLauncher()

        // Set up click listeners
        binding.buttonStart.setOnClickListener {
            requestNotificationPermission()
            AnalyticsEvents.logEvent(AnalyticsEvents.notification_activate_button_clic)

        }

        binding.buttonConfigureLater.setOnClickListener {
            goMain()
            AnalyticsEvents.logEvent(AnalyticsEvents.notification_deactivate_button_clic)
        }

        // Check if notifications are already enabled
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            goMain()
        }
    }

    private fun initializePermissionLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    goMain()
                } else {
                    goMain()  // Instead of redirecting to settings, just call goMain
                }
            }
        }
    }

    private fun initializeNotificationSettingsLauncher() {
        notificationSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // After returning from the notification settings
            goMain()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermission()
        } else {
            goMain()  // Directly call goMain if the OS version is lower than TIRAMISU
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationsPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun redirectToNotificationSettings() {
        // Always redirect to notification settings
        MainActivity.shouldLaunchOnboarding = true
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }
        notificationSettingsLauncher.launch(intent)
    }

    private fun goMain() {
        EntourageApplication.get().authenticationController.me?.let { me ->
            OnboardingAPI.getInstance().getUser(me.id) { isOK, userResponse ->
                if (isOK) {
                    if (userResponse != null) {
                        userResponse.user.phone = me.phone
                        EntourageApplication.get().authenticationController.saveUser(userResponse.user)
                    }
                }
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, true)
                    .apply()
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, false)
                    .apply()
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_MIGRATION_V7_OK, true)
                    .apply()
                MainActivity.shouldLaunchOnboarding = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
