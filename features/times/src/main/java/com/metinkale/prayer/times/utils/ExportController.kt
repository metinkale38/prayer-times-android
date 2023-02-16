/*
 * Copyright (c) 2013-2023 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.metinkale.prayer.times.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.*
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.drawableId
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.utils.LocaleUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ExportController {
    @Throws(IOException::class)
    fun exportPDF(ctx: Context, times: Times, from: LocalDate, to: LocalDate) {
        var date = from
        val document = PdfDocument()
        val pageInfo: PageInfo
        val pw = 595
        val ph = 842
        pageInfo = PageInfo.Builder(pw, ph, 1).create()
        val page: Page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.setARGB(255, 0, 0, 0)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER

        canvas.translate(0.0f, -15.0f)

        Drawable.createFromStream(ctx.assets.open("pdf/launcher.png"), null)?.let { launcher ->
            launcher.setBounds(30, 30, 30 + 65, 30 + 65)
            launcher.draw(canvas)
        }
        Drawable.createFromStream(ctx.assets.open("pdf/qrcode.png"), null)?.let { qr ->
            qr.setBounds(pw - 30 - 65, 40 + 65 + 5, pw - 30, 40 + 65 + 5 + 65)
            qr.draw(canvas)
        }
        Drawable.createFromStream(
            ctx.assets.open(
                "pdf/badge_" + LocaleUtils.getLanguage("en", "de", "tr", "fr", "ar") + ".png"
            ), null
        )?.let { badge ->
            val w = 100
            val h: Int = w * badge.intrinsicHeight / badge.intrinsicWidth
            badge.setBounds(pw - 30 - w, 30 + (60 / 2 - h / 2), pw - 30, 30 + (60 / 2 - h / 2) + h)
            badge.draw(canvas)
            canvas.drawText(
                "com.metinkale.prayer", pw - 30 - w / 2f, 30 + (60 / 2f - h / 2f) + h + 10, paint
            )


        }

        paint.setARGB(255, 61, 184, 230)
        canvas.drawRect(
            0f, (30 + 60).toFloat(), pw.toFloat(), (30 + 60 + 5).toFloat(), paint
        )


        times.source.drawableId?.let {
            ContextCompat.getDrawable(ctx, it)?.let { source ->
                val h = 65
                val w = h * source.intrinsicWidth / source.intrinsicHeight
                source.setBounds(30, 40 + 65 + 5, 30 + w, 40 + 65 + 5 + h)
                source.draw(canvas)
            }
        }
        paint.setARGB(255, 0, 0, 0)
        paint.textSize = 30f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(
            ctx.getText(R.string.appName).toString(),
            (30 + 65 + 5).toFloat(),
            (30 + 50).toFloat(),
            paint
        )


        val p = 30

        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText(times.name, pw / 2.0f, (40 + 65 + 50).toFloat(), paint)
        paint.textSize = 12f
        var y = 40 + 65 + 5 + 65 + 30


        paint.isFakeBoldText = false
        val dateWidth = paint.measureText("00.00.0000")
        val timeWidth = paint.measureText("00:00")
        val width = pw - p - p
        val alignWidth = width - dateWidth - timeWidth
        val xPos: List<Pair<Paint.Align, Float>> =
            listOf(Paint.Align.LEFT to 0f) +
                    (1..5).map {
                        Paint.Align.CENTER to (dateWidth - timeWidth / 2) + it.toFloat() * ((alignWidth + timeWidth) / 6)
                    } +
                    listOf(Paint.Align.RIGHT to width.toFloat())

        paint.isFakeBoldText = true

        paint.setARGB(255, 61, 184, 230)
        canvas.drawRect(0f, -15f + y.toFloat(), pw.toFloat(), -15f + y + 20f, paint)

        paint.color = Color.WHITE

        xPos.forEachIndexed { index, (align, x) ->
            paint.textAlign = align

            val text = when (index) {
                0 -> ctx.getString(R.string.date)
                1 -> Vakit.FAJR.string
                2 -> Vakit.SUN.string
                3 -> Vakit.DHUHR.string
                4 -> Vakit.ASR.string
                5 -> Vakit.MAGHRIB.string
                6 -> Vakit.ISHAA.string
                else -> ""
            }
            canvas.drawText(text, p + x, y.toFloat(), paint)

        }


        paint.isFakeBoldText = false
        var i = 0
        do {
            y += 20

            if (++i % 2 == 0) {
                paint.setARGB(100, 61, 184, 230)
                canvas.drawRect(0f, -15f + y.toFloat(), pw.toFloat(), -15f + y + 20f, paint)
            }
            paint.color = Color.BLACK

            xPos.forEachIndexed { index, (align, x) ->
                paint.textAlign = align
                val text = when (index) {
                    0 -> date.toString("dd.MM.yyyy")
                    1 -> times.getTime(date, Vakit.FAJR.ordinal).toLocalTime().toString("HH:mm")
                    2 -> times.getTime(date, Vakit.SUN.ordinal).toLocalTime().toString("HH:mm")
                    3 -> times.getTime(date, Vakit.DHUHR.ordinal).toLocalTime().toString("HH:mm")
                    4 -> times.getTime(date, Vakit.ASR.ordinal).toLocalTime().toString("HH:mm")
                    5 -> times.getTime(date, Vakit.MAGHRIB.ordinal).toLocalTime().toString("HH:mm")
                    6 -> times.getTime(date, Vakit.ISHAA.ordinal).toLocalTime().toString("HH:mm")
                    else -> ""
                }
                canvas.drawText(text, p + x, y.toFloat(), paint)
            }
        } while (!date.plusDays(1).also { date = it }.isAfter(to))
        document.finishPage(page)
        val outputDir = ctx.cacheDir
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, times.name.replace(" ", "_") + ".pdf")
        if (outputFile.exists()) outputFile.delete()
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        document.close()
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "application/pdf"
        val uri: Uri = FileProvider.getUriForFile(
            ctx, ctx.getString(R.string.FILE_PROVIDER_AUTHORITIES), outputFile
        )
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        ctx.startActivity(Intent.createChooser(shareIntent, ctx.resources.getText(R.string.export)))
    }

    @Throws(IOException::class)
    fun exportCSV(ctx: Context, times: Times, from: LocalDate, to: LocalDate) {
        var date = from
        val outputDir = ctx.cacheDir
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, times.name.replace(" ", "_") + ".csv")
        if (outputFile.exists()) outputFile.delete()
        val outputStream = FileOutputStream(outputFile)
        outputStream.write("HijriDate;Fajr;Shuruq;Dhuhr;Asr;Maghrib;Ishaa\n".toByteArray())
        do {
            outputStream.write((date.toString("yyyy-MM-dd") + ";").toByteArray())
            outputStream.write(
                (times.getTime(date, Vakit.FAJR.ordinal).toLocalTime()
                    .toString("HH:mm") + ";").toByteArray()
            )
            outputStream.write(
                (times.getTime(date, Vakit.SUN.ordinal).toLocalTime()
                    .toString("HH:mm") + ";").toByteArray()
            )
            outputStream.write(
                (times.getTime(date, Vakit.DHUHR.ordinal).toLocalTime()
                    .toString("HH:mm") + ";").toByteArray()
            )
            outputStream.write(
                (times.getTime(date, Vakit.ASR.ordinal).toLocalTime()
                    .toString("HH:mm") + ";").toByteArray()
            )
            outputStream.write(
                (times.getTime(date, Vakit.MAGHRIB.ordinal).toLocalTime()
                    .toString("HH:mm") + ";").toByteArray()
            )
            outputStream.write(
                """${times.getTime(date, Vakit.ISHAA.ordinal).toLocalTime().toString("HH:mm")}
""".toByteArray()
            )
        } while (!date.plusDays(1).also { date = it }.isAfter(to))
        outputStream.close()
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "name/csv"
        val uri: Uri = FileProvider.getUriForFile(
            ctx, ctx.getString(R.string.FILE_PROVIDER_AUTHORITIES), outputFile
        )
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        ctx.startActivity(Intent.createChooser(shareIntent, ctx.resources.getText(R.string.export)))
    }

    fun export(ctx: Context, times: Times) {
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle(R.string.export).setItems(
            arrayOf<CharSequence>("CSV", "PDF")
        ) { _: DialogInterface?, which: Int ->
            var minDate: Long = 0
            var maxDate = Long.MAX_VALUE
            (times.dayTimes as? DayTimesWebProvider)?.let {
                minDate = it.firstSyncedDay?.toDateTimeAtCurrentTime()?.millis ?: 0
                maxDate = it.lastSyncedDay?.toDateTimeAtCurrentTime()?.millis ?: 0
            }

            val ld = LocalDate.now()
            val finalMaxDate = maxDate
            val dlg = DatePickerDialog(
                ctx, { _: DatePicker?, y: Int, m: Int, d: Int ->
                    val from = LocalDate(y, m + 1, d)
                    val dlg1 = DatePickerDialog(
                        ctx, { _: DatePicker?, y1: Int, m1: Int, d1: Int ->
                            val to = LocalDate(y1, m1 + 1, d1)
                            try {
                                if (which == 0) {
                                    exportCSV(ctx, times, from, to)
                                } else {
                                    exportPDF(ctx, times, from, to)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                CrashReporter.recordException(e)
                                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show()
                            }
                        }, ld.year, ld.monthOfYear - 1, ld.dayOfMonth
                    )
                    val startDate = DateTime.now().withDate(y, m + 1, d)
                    val start = startDate.millis
                    if (which == 1) dlg1.datePicker.maxDate = Math.min(
                        finalMaxDate, startDate.plusDays(31).millis
                    ) else dlg1.datePicker.maxDate = finalMaxDate
                    dlg1.datePicker.minDate = Math.min(
                        start, dlg1.datePicker.maxDate - 1000 * 60 * 60 * 24
                    )
                    dlg1.setTitle(R.string.to)
                    dlg1.show()
                }, ld.year, ld.monthOfYear - 1, ld.dayOfMonth
            )
            dlg.datePicker.minDate = minDate
            dlg.datePicker.maxDate = maxDate.coerceAtLeast(minDate)
            dlg.setTitle(R.string.from)
            dlg.show()
        }
        builder.show()
    }
}