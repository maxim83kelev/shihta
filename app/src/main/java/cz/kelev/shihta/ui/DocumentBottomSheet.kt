package cz.kelev.shihta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheet(
    title: String,
    fileName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val text = remember {
        try {
            context.assets.open(fileName).bufferedReader().readText()
        } catch (e: Exception) {
            "Dokument není k dispozici."
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ColorBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}