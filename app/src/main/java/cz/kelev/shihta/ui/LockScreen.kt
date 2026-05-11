package cz.kelev.shihta.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun LockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("shihta_prefs", android.content.Context.MODE_PRIVATE)
    val savedPin = prefs.getString("pin_code", "") ?: ""
    val biometricEnabled = prefs.getBoolean("biometric_enabled", false)

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }

    // Пробуем биометрию сразу при открытии
    LaunchedEffect(Unit) {
        if (biometricEnabled && savedPin.isNotBlank()) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(
                    context as FragmentActivity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            onUnlocked()
                        }
                        override fun onAuthenticationFailed() {
                            showPinInput = true
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            showPinInput = true
                        }
                    }
                )
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Šichta")
                    .setSubtitle("Ověřte svou identitu")
                    .setNegativeButtonText("PIN")
                    .build()
                prompt.authenticate(info)
            } else {
                showPinInput = true
            }
        } else {
            showPinInput = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Šichta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrown
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Zadejte PIN",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 6) pin = it
                    if (it.length == savedPin.length && it == savedPin) {
                        onUnlocked()
                    } else if (it.length == savedPin.length) {
                        error = "Špatný PIN"
                        pin = ""
                    }
                },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = Color.Red, fontSize = 13.sp)
            }
        }
    }
}