package cz.kelev.shihta.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import cz.kelev.shihta.db.ShiftEntry

@Composable
fun QrScreen(
    vm: ShiftViewModel,
    userName: String,
    onBack: () -> Unit
) {
    val entries by vm.entries.collectAsState()
    val selectedYear by vm.selectedYear.collectAsState()
    val selectedMonth by vm.selectedMonth.collectAsState()
    val months = listOf(
        "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
        "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
    )

    val qrBitmap = remember(entries, selectedYear, selectedMonth) {
        generateQrBitmap(
            entries = entries,
            userName = userName,
            monthName = months[selectedMonth - 1],
            year = selectedYear,
            daysInMonth = vm.daysInCurrentMonth
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .systemBarsPadding()
    ) {
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
            Text("QR kód", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${userName.ifBlank { "Šichta" }} — ${months[selectedMonth - 1]} $selectedYear",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrown
            )
            Spacer(modifier = Modifier.height(24.dp))

            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR kód",
                    modifier = Modifier.size(280.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Naskenujte pro zobrazení výkazu",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

fun generateQrBitmap(
    entries: List<ShiftEntry>,
    userName: String,
    monthName: String,
    year: Int,
    daysInMonth: Int
): Bitmap? {
    return try {
        val entryMap = entries.associateBy { it.day }
        val sb = StringBuilder()
        sb.append("$userName | $monthName $year\n")
        for (day in 1..daysInMonth) {
            val e = entryMap[day]
            if (e != null && (e.stavba.isNotBlank() || e.hodiny.isNotBlank())) {
                sb.append("$day: ${e.stavba} ${e.hodiny}h ${e.poznamky}\n")
            }
        }

        val writer = QRCodeWriter()
        val matrix = writer.encode(sb.toString(), BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}