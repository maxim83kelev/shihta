package cz.kelev.shihta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val readmeText = remember {
        try {
            context.assets.open("readme.txt").bufferedReader().readText()
        } catch (e: Exception) {
            "Vítejte v aplikaci Šichta."
        }
    }

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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Šichta",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Текст readme
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                readmeText,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )
        }

        // Кнопка
        Button(
            onClick = {
                context.getSharedPreferences("shihta_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().putBoolean("onboarding_done", true).apply()
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
        ) {
            Text("Začít", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}