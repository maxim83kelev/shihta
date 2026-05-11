package cz.kelev.shihta.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

@Composable
fun SignatureScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val paths = remember { mutableStateListOf<List<Offset>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }
    var saved by remember { mutableStateOf(false) }

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
            Text("Podpis", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Podepište se prstem",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Поле для подписи
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
                .border(1.dp, ColorBorder)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath.clear()
                                currentPath.add(offset)
                            },
                            onDrag = { change, _ ->
                                currentPath.add(change.position)
                            },
                            onDragEnd = {
                                paths.add(currentPath.toList())
                                currentPath.clear()
                            }
                        )
                    }
            ) {
                // Рисуем сохранённые линии
                paths.forEach { path ->
                    for (i in 0 until path.size - 1) {
                        drawLine(
                            color = Color.Black,
                            start = path[i],
                            end = path[i + 1],
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Рисуем текущую линию
                for (i in 0 until currentPath.size - 1) {
                    drawLine(
                        color = Color.Black,
                        start = currentPath[i],
                        end = currentPath[i + 1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    paths.clear()
                    currentPath.clear()
                    saved = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Vymazat")
            }

            Button(
                onClick = {
                    val bmpWidth = 600
                    val bmpHeight = 200
                    val bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    val paint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = 4f
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                    }

                    // Находим реальные границы подписи
                    val allPoints = paths.flatten()
                    if (allPoints.isNotEmpty()) {
                        val minX = allPoints.minOf { it.x }
                        val maxX = allPoints.maxOf { it.x }
                        val minY = allPoints.minOf { it.y }
                        val maxY = allPoints.maxOf { it.y }
                        val rangeX = (maxX - minX).coerceAtLeast(1f)
                        val rangeY = (maxY - minY).coerceAtLeast(1f)

                        val margin = 20f
                        val scaleX = (bmpWidth - margin * 2) / rangeX
                        val scaleY = (bmpHeight - margin * 2) / rangeY
                        val scale = minOf(scaleX, scaleY)

                        paths.forEach { path ->
                            for (i in 0 until path.size - 1) {
                                canvas.drawLine(
                                    (path[i].x - minX) * scale + margin,
                                    (path[i].y - minY) * scale + margin,
                                    (path[i + 1].x - minX) * scale + margin,
                                    (path[i + 1].y - minY) * scale + margin,
                                    paint
                                )
                            }
                        }
                    }

                    val file = File(context.filesDir, "signature.png")
                    FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    saved = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
            ) {
                Text(if (saved) "Uloženo ✓" else "Uložit podpis")
            }
        }
    }
}