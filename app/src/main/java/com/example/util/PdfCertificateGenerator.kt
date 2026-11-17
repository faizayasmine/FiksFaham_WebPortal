package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TAHAP 8C: Generator Dokumen PDF Sertifikat Digital FiksFaham.
 * Menggunakan PdfDocument bawaan Android tanpa logo, tanda tangan, atau ID karangan.
 */
object PdfCertificateGenerator {

    private const val PAGE_WIDTH = 842 // A4 Landscape width in PostScript points
    private const val PAGE_HEIGHT = 595 // A4 Landscape height in PostScript points

    fun generateCertificatePdf(
        context: Context,
        userName: String,
        materiTitle: String,
        materiId: Int,
        posttestScore: Int,
        capstoneScore: Int,
        issueDateMillis: Long = System.currentTimeMillis(),
        certificateNumber: String? = null
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 1. Background Fill
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FAF7F2")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // 2. Decorative Double Border
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor("#E25A20") // FiksPrimary Orange
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), outerBorderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#D0C4B4") // Subtle neutral border
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRect(32f, 32f, (PAGE_WIDTH - 32).toFloat(), (PAGE_HEIGHT - 32).toFloat(), innerBorderPaint)

        // 3. Brand Header "FIKSFAHAM"
        val brandPaint = Paint().apply {
            color = Color.parseColor("#E25A20")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("FIKSFAHAM", (PAGE_WIDTH / 2).toFloat(), 85f, brandPaint)

        // Sub-brand label
        val subBrandPaint = Paint().apply {
            color = Color.parseColor("#6B5A4D")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("PLATFORM PEMBELAJARAN DESAIN MULTIDISIPLIN FIKSFAHAM", (PAGE_WIDTH / 2).toFloat(), 105f, subBrandPaint)

        // 4. Main Certificate Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1713")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SERTIFIKAT KELULUSAN", (PAGE_WIDTH / 2).toFloat(), 160f, titlePaint)

        // 5. Awarded to Statement
        val statementPaint = Paint().apply {
            color = Color.parseColor("#6B5A4D")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Diberikan kepada mahasiswa:", (PAGE_WIDTH / 2).toFloat(), 200f, statementPaint)

        // 6. Recipient Name
        val namePaint = Paint().apply {
            color = Color.parseColor("#1E1713")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(userName.ifBlank { "Peserta FiksFaham" }, (PAGE_WIDTH / 2).toFloat(), 250f, namePaint)

        // Name underline accent
        val linePaint = Paint().apply {
            color = Color.parseColor("#E25A20")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val textWidth = namePaint.measureText(userName.ifBlank { "Peserta FiksFaham" })
        val lineStartX = (PAGE_WIDTH / 2) - (textWidth / 2) - 20
        val lineEndX = (PAGE_WIDTH / 2) + (textWidth / 2) + 20
        canvas.drawLine(lineStartX, 265f, lineEndX, 265f, linePaint)

        // 7. Course Achievement Statement
        canvas.drawText("Atas keberhasilan menyelesaikan seluruh modul pembelajaran, evaluasi, dan Capstone Project pada:", (PAGE_WIDTH / 2).toFloat(), 305f, statementPaint)

        // 8. Materi Title Box
        val materiCardPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val materiCardBorder = Paint().apply {
            color = Color.parseColor("#E8DFD3")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val cardRect = RectF(100f, 325f, (PAGE_WIDTH - 100).toFloat(), 375f)
        canvas.drawRoundRect(cardRect, 10f, 10f, materiCardPaint)
        canvas.drawRoundRect(cardRect, 10f, 10f, materiCardBorder)

        val materiTitlePaint = Paint().apply {
            color = Color.parseColor("#E25A20")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(materiTitle, (PAGE_WIDTH / 2).toFloat(), 356f, materiTitlePaint)

        // 9. Evaluation Recap Scores
        val scorePaint = Paint().apply {
            color = Color.parseColor("#25201C")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val scoreText = "Nilai Evaluasi:  Posttest: $posttestScore/100  |  Capstone Project: $capstoneScore/100  (Status: LULUS)"
        canvas.drawText(scoreText, (PAGE_WIDTH / 2).toFloat(), 410f, scorePaint)

        // 10. Date, Verification Number and Official Issuer Note
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val formattedDate = dateFormat.format(Date(issueDateMillis))
        val dateText = "Diterbitkan pada: $formattedDate"
        canvas.drawText(dateText, (PAGE_WIDTH / 2).toFloat(), 440f, statementPaint)
        if (!certificateNumber.isNullOrBlank()) {
            val certIdPaint = Paint().apply {
                color = Color.parseColor("#E25A20")
                textSize = 12f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("No. Sertifikat Terverifikasi: $certificateNumber", (PAGE_WIDTH / 2).toFloat(), 466f, certIdPaint)
        }

        // 11. Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#8E8074")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "Dokumen sertifikat elektronik diterbitkan secara otomatis oleh sistem FiksFaham. Tanpa rekayasa tanda tangan atau nomor sertifikat fiktif.",
            (PAGE_WIDTH / 2).toFloat(),
            520f,
            footerPaint
        )
        canvas.drawText(
            "FiksFaham • Universitas Negeri Surabaya (UNESA)",
            (PAGE_WIDTH / 2).toFloat(),
            538f,
            footerPaint
        )

        document.finishPage(page)

        // Save PDF to cache directory inside 'certificates' subfolder
        val certDir = File(context.cacheDir, "certificates").apply {
            if (!exists()) mkdirs()
        }
        val sanitizedMateri = "Materi_$materiId"
        val outputFile = File(certDir, "Sertifikat_FiksFaham_${sanitizedMateri}.pdf")

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    /**
     * Membagikan file PDF sertifikat melalui FileProvider
     */
    fun shareCertificate(context: Context, file: File, materiTitle: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sertifikat Kelulusan $materiTitle - FiksFaham")
            putExtra(Intent.EXTRA_TEXT, "Berikut adalah sertifikat kelulusan $materiTitle dari FiksFaham.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Bagikan Sertifikat FiksFaham")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
