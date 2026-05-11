package cz.kelev.shihta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: ShiftViewModel,
    onBack: () -> Unit,
    onQrClick: () -> Unit = {},
    onSignatureClick: () -> Unit = {}
) {
    val prefs = vm.prefs
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    val selectedYear by vm.selectedYear.collectAsState()
    val selectedMonth by vm.selectedMonth.collectAsState()
    val months = listOf(
        "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
        "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
    )

    var showAboutSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showEulaSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }
    var showDateSheet by remember { mutableStateOf(false) }
    var showSaveSheet by remember { mutableStateOf(false) }
    var showSecuritySheet by remember { mutableStateOf(false) }
    var showSummarySheet by remember { mutableStateOf(false) }
    var showAlarmSheet by remember { mutableStateOf(false) }
    var showSendReminderSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .systemBarsPadding()
    ) {
        // Хедер
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorBrown)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zpět", tint = Color.White)
            }
            Text("Nastavení", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 200.dp)
        ) {
            SettingsButton("O aplikaci", "Verze ${cz.kelev.shihta.BuildConfig.VERSION_NAME}") { showAboutSheet = true }
            SettingsButton("Zásady ochrany osobních údajů", "Privacy Policy") { showPrivacySheet = true }
            SettingsButton("Licenční smlouva", "EULA") { showEulaSheet = true }
            SettingsButton("Jméno / Příjmení", userName.ifBlank { "Nezadáno" }) { showNameSheet = true }
            SettingsButton("Měsíc", "${months[selectedMonth - 1]} $selectedYear") { showDateSheet = true }
            SettingsButton("Uložit na zařízení", "PDF nebo Excel") { showSaveSheet = true }
            SettingsButton("Zabezpečení", "PIN / Biometrie") { showSecuritySheet = true }
            SettingsButton("Připomínka", "Denní upozornění") { showAlarmSheet = true }
            SettingsButton("Připomínka odeslání", "Odeslat výkaz zaměstnavateli") { showSendReminderSheet = true }
            SettingsButton("QR kód", "Sdílet výkaz přes QR") { onQrClick() }
            SettingsButton("Souhrn a výdělek", "Hodiny a sazba") { showSummarySheet = true }
            val signatureEnabled = remember { mutableStateOf(prefs.getBoolean("signature_enabled", false)) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).clickable { 
                        if (signatureEnabled.value) onSignatureClick() 
                    }
                ) {
                    Text("Podpis", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (signatureEnabled.value) "Zapnuto — klepněte pro změnu" else "Vypnuto",
                        fontSize = 13.sp, color = Color.Gray
                    )
                }
                Switch(
                    checked = signatureEnabled.value,
                    onCheckedChange = {
                        signatureEnabled.value = it
                        prefs.edit().putBoolean("signature_enabled", it).apply()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }
            HorizontalDivider(color = ColorBorder)
        }
    }

    //O aplikaci
    if (showAboutSheet) {
        AboutBottomSheet(onDismiss = { showAboutSheet = false })
    }

    //privacy_policy
    if (showPrivacySheet) {
        DocumentBottomSheet(
            title = "Zásady ochrany osobních údajů",
            fileName = "privacy_policy.txt",
            onDismiss = { showPrivacySheet = false }
        )
    }

    //Eula
    if (showEulaSheet) {
        DocumentBottomSheet(
            title = "Licenční smlouva",
            fileName = "eula.txt",
            onDismiss = { showEulaSheet = false }
        )
    }

    // Bottom Sheet — Имя
    if (showNameSheet) {
        ModalBottomSheet(onDismissRequest = { showNameSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Jméno / Příjmení", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        prefs.edit().putString("user_name", it).apply()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("Vaše jméno") }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Bottom Sheet — Месяц
    if (showDateSheet) {
        ModalBottomSheet(onDismissRequest = { showDateSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Měsíc", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newMonth = if (selectedMonth == 1) 12 else selectedMonth - 1
                        val newYear = if (selectedMonth == 1) selectedYear - 1 else selectedYear
                        vm.setSelectedMonth(newYear, newMonth)
                    }) { Text("◀", fontSize = 24.sp, color = ColorBrown) }
                    Text("${months[selectedMonth - 1]} $selectedYear", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorBrown)
                    IconButton(onClick = {
                        val newMonth = if (selectedMonth == 12) 1 else selectedMonth + 1
                        val newYear = if (selectedMonth == 12) selectedYear + 1 else selectedYear
                        vm.setSelectedMonth(newYear, newMonth)
                    }) { Text("▶", fontSize = 24.sp, color = ColorBrown) }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Bottom Sheet — Сохранить
    if (showSaveSheet) {
        SaveBottomSheet(vm = vm, userName = userName, onDismiss = { showSaveSheet = false })
    }

    // Bottom Sheet — Безопасность
    if (showSecuritySheet) {
        SecurityBottomSheet(prefs = prefs, onDismiss = { showSecuritySheet = false })
    }

    //alarm
    if (showAlarmSheet) {
        AlarmBottomSheet(onDismiss = { showAlarmSheet = false })
    }

    //Připomínka odeslání
    if (showSendReminderSheet) {
        SendReminderBottomSheet(onDismiss = { showSendReminderSheet = false })
    }

    if (showSummarySheet) {
        SummarySettingsBottomSheet(vm = vm, onDismiss = { showSummarySheet = false })
    }
    
}

@Composable
fun SettingsButton(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    HorizontalDivider(color = ColorBorder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityBottomSheet(
    prefs: android.content.SharedPreferences,
    onDismiss: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var pinSaved by remember { mutableStateOf(false) }
    val biometricEnabled = remember { mutableStateOf(prefs.getBoolean("biometric_enabled", false)) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Zabezpečení", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            var pinVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it },
                    label = { Text("Nový PIN (4-6 číslic)") },
                    visualTransformation = if (pinVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                        else PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible) androidx.compose.material.icons.Icons.Default.VisibilityOff
                                            else androidx.compose.material.icons.Icons.Default.Visibility,
                                contentDescription = null,
                                tint = ColorBrown
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (newPin.length >= 4) {
                        prefs.edit().putString("pin_code", newPin).apply()
                        pinSaved = true
                        newPin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
            ) {
                Text(if (pinSaved) "PIN uložen ✓" else "Uložit PIN")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Biometrie (otisk prstu)", fontSize = 14.sp)
                Switch(
                    checked = biometricEnabled.value,
                    onCheckedChange = {
                        biometricEnabled.value = it
                        prefs.edit().putBoolean("biometric_enabled", it).apply()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}