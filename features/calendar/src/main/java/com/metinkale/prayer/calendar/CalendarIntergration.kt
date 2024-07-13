package com.metinkale.prayer.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.date.HijriDay
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.LocaleUtils.getHolyday
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate


fun runCalendarIntegration(ctx: Context) {

    val prodId = "prayer-times-android/${ctx.packageName}"


    val dtstamp = LocalDate.now().toString().replace("-", "") + "T000000"
    val output = StringBuilder().apply {
        appendLine("BEGIN:VCALENDAR")
        appendLine("VERSION:2.0")
        appendLine("PRODID:-//$prodId")
        appendLine("METHOD:PUBLISH")
        for (year in LocalDate.now().year..HijriDate.MAX_GREG_YEAR) {
            for (date in HijriDate.getHolydaysForGregYear(year)) {
                if (date.second == HijriDay.MONTH) continue
                appendLine("BEGIN:VEVENT")
                appendLine("TRANSP:TRANSPARENT")
                appendLine("SUMMARY:" + getHolyday(date.second))
                appendLine("DTSTAMP:$dtstamp")
                appendLine("UID:${year}${date.second}@${ctx.packageName}")
                appendLine(
                    "DTSTART;VALUE=DATE:" + date.first.toLocalDate().toString().replace("-", "")
                )
                appendLine("DESCRIPTION:${LocaleUtils.formatDate(date.first)}")
                appendLine("URL;VALUE=URI:https://play.google.com/store/apps/details?id=com.metinkale.prayer")
                appendLine("END:VEVENT")
            }
        }
        appendLine("END:VCALENDAR")
    }.toString()

    val outputDir = ctx.cacheDir
    if (!outputDir.exists()) outputDir.mkdirs()
    val outputFile = File(outputDir, "holydays.ics")
    if (outputFile.exists()) outputFile.delete()
    FileOutputStream(outputFile).bufferedWriter().use { it.write(output) }

    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    val uri: Uri = FileProvider.getUriForFile(
        ctx, ctx.getString(com.metinkale.prayer.base.R.string.FILE_PROVIDER_AUTHORITIES), outputFile
    )
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    intent.setDataAndType(uri, "text/calendar")
    ctx.startActivity(intent)
}