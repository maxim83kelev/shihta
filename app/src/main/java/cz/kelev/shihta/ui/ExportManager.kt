package cz.kelev.shihta.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import cz.kelev.shihta.db.ShiftEntry
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExportManager {

    // ─── Шаринг ───────────────────────────────────────────────────────────────

    fun exportExcel(
        context: Context,
        entries: List<ShiftEntry>,
        userName: String,
        monthName: String,
        year: Int,
        daysInMonth: Int
    ) {
        val fileName = "${userName.ifBlank { "Shihta" }}_${monthName}_${year}.xlsx"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { buildExcel(it, entries, userName, monthName, year, daysInMonth) }
        shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    fun exportPdf(
        context: Context,
        entries: List<ShiftEntry>,
        userName: String,
        monthName: String,
        year: Int,
        daysInMonth: Int
    ) {
        val fileName = "${userName.ifBlank { "Shihta" }}_${monthName}_${year}.pdf"
        val file = File(context.cacheDir, fileName)
        buildPdf(context, file, entries, userName, monthName, year, daysInMonth)
        shareFile(context, file, "application/pdf")
    }

    // ─── Сохранение в папку ───────────────────────────────────────────────────

    fun saveToFolder(
        context: Context,
        treeUri: android.net.Uri,
        entries: List<ShiftEntry>,
        userName: String,
        monthName: String,
        year: Int,
        daysInMonth: Int,
        format: String
    ) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return

        // Создаём папку Šichta если нет
        val folder = root.findFile("Sichta") ?: root.createDirectory("Sichta") ?: return

        val name = "${userName.ifBlank { "Shihta" }}_${monthName}_${year}"
        val mimeType = if (format == "pdf") "application/pdf"
            else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        val fileName = "$name.$format"

        // Удаляем старый файл если есть
        folder.findFile(fileName)?.delete()

        val docFile = folder.createFile(mimeType, fileName) ?: return

        context.contentResolver.openOutputStream(docFile.uri)?.use { stream ->
            if (format == "pdf") {
                val temp = File(context.cacheDir, fileName)
                buildPdf(context, temp, entries, userName, monthName, year, daysInMonth)
                temp.inputStream().copyTo(stream)
            } else {
                buildExcel(stream, entries, userName, monthName, year, daysInMonth)
            }
        }

        android.widget.Toast.makeText(
            context,
            "Uloženo do složky Sichta",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    // ─── Внутренние методы ────────────────────────────────────────────────────

    private fun buildExcel(
        outputStream: java.io.OutputStream,
        entries: List<ShiftEntry>,
        userName: String,
        monthName: String,
        year: Int,
        daysInMonth: Int
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("$monthName $year")
        val headerRow = sheet.createRow(0)
        listOf("Datum", "Stavba", "Hodiny", "Poznámky").forEachIndexed { i, title ->
            headerRow.createCell(i).setCellValue(title)
        }
        val entryMap = entries.associateBy { it.day }
        for (day in 1..daysInMonth) {
            val row = sheet.createRow(day)
            val entry = entryMap[day]
            row.createCell(0).setCellValue(day.toString())
            row.createCell(1).setCellValue(entry?.stavba ?: "")
            row.createCell(2).setCellValue(entry?.hodiny ?: "")
            row.createCell(3).setCellValue(entry?.poznamky ?: "")
        }
        workbook.write(outputStream)
        workbook.close()
    }
    private fun buildPdf(
        context: Context,
        file: File,
        entries: List<ShiftEntry>,
        userName: String,
        monthName: String,
        year: Int,
        daysInMonth: Int
    ) {
        val pageWidth = 595
        val marginLeft = 30f
        val marginTop = 60f
        val rowHeight = 22f
        val colWidths = floatArrayOf(40f, 180f, 70f, 245f)

        val pdfDoc = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paintBrown = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(107, 66, 38)
            style = android.graphics.Paint.Style.FILL
        }
        val paintHeader = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(139, 94, 60)
            style = android.graphics.Paint.Style.FILL
        }
        val paintBorder = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(208, 192, 168)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        val paintTextWhite = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val paintTextBlack = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }
        val paintToday = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(255, 241, 118)
            style = android.graphics.Paint.Style.FILL
        }

        canvas.drawRect(marginLeft, 15f, marginLeft + colWidths.sum(), 40f, paintBrown)
        canvas.drawText(if (userName.isBlank()) "Šichta" else userName, marginLeft + 8f, 32f, paintTextWhite)
        canvas.drawText("$monthName $year", marginLeft + colWidths.sum() - 100f, 32f, paintTextWhite)

        var x = marginLeft
        listOf("Datum", "Stavba", "Hodiny", "Poznámky").forEachIndexed { i, title ->
            canvas.drawRect(x, marginTop, x + colWidths[i], marginTop + rowHeight, paintHeader)
            canvas.drawRect(x, marginTop, x + colWidths[i], marginTop + rowHeight, paintBorder)
            canvas.drawText(title, x + 4f, marginTop + 15f, paintTextWhite)
            x += colWidths[i]
        }

        val entryMap = entries.associateBy { it.day }
        val now = java.util.Calendar.getInstance()
        val todayDay = now.get(java.util.Calendar.DAY_OF_MONTH)
        val todayMonth = now.get(java.util.Calendar.MONTH) + 1
        val todayYear = now.get(java.util.Calendar.YEAR)

        for (day in 1..daysInMonth) {
            val rowTop = marginTop + rowHeight + (day - 1) * rowHeight
            val entry = entryMap[day]
            val isToday = day == todayDay && todayMonth == entries.firstOrNull()?.month && todayYear == year
            x = marginLeft
            colWidths.forEachIndexed { i, w ->
                if (isToday) canvas.drawRect(x, rowTop, x + w, rowTop + rowHeight, paintToday)
                canvas.drawRect(x, rowTop, x + w, rowTop + rowHeight, paintBorder)
                val text = when (i) {
                    0 -> day.toString()
                    1 -> entry?.stavba ?: ""
                    2 -> entry?.hodiny ?: ""
                    3 -> entry?.poznamky ?: ""
                    else -> ""
                }
                canvas.drawText(text, x + 4f, rowTop + 15f, paintTextBlack)
                x += w
            }
        }

        // Подпись
        val signEnabled = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)
            .getBoolean("signature_enabled", false)
        val signatureFile = File(context.filesDir, "signature.png")

        if (signEnabled && signatureFile.exists()) {
            val signBmp = android.graphics.BitmapFactory.decodeFile(signatureFile.absolutePath)
            val signTop = marginTop + rowHeight + daysInMonth * rowHeight + 15f
            val paintSign = android.graphics.Paint()
            canvas.drawText("Podpis:", marginLeft, signTop + 14f, paintTextBlack)
            // Рисуем подпись с правильными пропорциями
            val signWidth = 100f
            val signHeight = signWidth * signBmp.height / signBmp.width
            canvas.drawBitmap(
                signBmp, null,
                android.graphics.RectF(marginLeft + 55f, signTop, marginLeft + 55f + signWidth, signTop + signHeight),
                paintSign
            )
        }

        pdfDoc.finishPage(page)
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
    }
    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться"))
    }
}