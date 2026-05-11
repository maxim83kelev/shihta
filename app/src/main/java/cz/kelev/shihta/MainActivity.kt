package cz.kelev.shihta

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kelev.shihta.ui.LockScreen
import cz.kelev.shihta.ui.QrScreen
import cz.kelev.shihta.ui.SettingsScreen
import cz.kelev.shihta.ui.ShiftScreen
import cz.kelev.shihta.ui.ShiftViewModel
import cz.kelev.shihta.ui.theme.ShihtaTheme
import androidx.compose.runtime.LaunchedEffect
import cz.kelev.shihta.ui.OnboardingScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        androidx.core.app.ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            1001
                        )
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(android.app.AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        )
                        startActivity(intent)
                    }
                }
            }
            ShihtaTheme {
                val vm: ShiftViewModel = viewModel()
                val prefs = vm.prefs
                val pinSet = prefs.getString("pin_code", "")?.isNotBlank() == true

                var unlocked by remember { mutableStateOf(!pinSet) }
                val onboardingDone = prefs.getBoolean("onboarding_done", false)
                var showOnboarding by remember { mutableStateOf(!onboardingDone) }
                var showSettings by remember { mutableStateOf(false) }
                var showQr by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
                var showSignature by remember { mutableStateOf(false) }

                when {
                    showOnboarding -> OnboardingScreen(onFinish = { showOnboarding = false })
                    !unlocked -> LockScreen(onUnlocked = { unlocked = true })
                    showSignature -> cz.kelev.shihta.ui.SignatureScreen(onBack = { showSignature = false })
                    showQr -> QrScreen(vm = vm, userName = userName, onBack = { showQr = false })
                    showSettings -> SettingsScreen(
                        vm = vm,
                        onBack = {
                            userName = prefs.getString("user_name", "") ?: ""
                            showSettings = false
                        },
                        onQrClick = { showQr = true },
                        onSignatureClick = { showSignature = true }
                    )
                    else -> ShiftScreen(
                        vm = vm,
                        userName = userName,
                        onSettingsClick = { showSettings = true },
                        onShareClick = {}
                    )
                }
            }
        }
    }
}