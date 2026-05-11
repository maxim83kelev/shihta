package cz.kelev.shihta.ui

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
import cz.kelev.shihta.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val readmeText = remember {
        try {
            context.assets.open("readme.txt").bufferedReader().readText()
        } catch (e: Exception) {
            "Popis není k dispozici."
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("O aplikaci", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "v${BuildConfig.VERSION_NAME}",
                    fontSize = 14.sp,
                    color = ColorBrown,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorBorder)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(readmeText, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}