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
package com.metinkale.prayer.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Preferences.UUID
import com.metinkale.prayer.base.R

object AboutShortcuts {
    @JvmStatic
    fun share(ctx: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.shareText))
        sendIntent.type = "name/plain"
        ctx.startActivity(sendIntent)
    }

    private fun openUrl(ctx: Context, url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(ctx.resources.getColor(R.color.colorPrimary))
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(ctx, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            ctx.startActivity(i)
        }
    }

    @JvmStatic
    fun github(ctx: Context) {
        val url = "https://github.com/metinkale38/prayer-times-android"
        openUrl(ctx, url)
    }

    @JvmStatic
    fun rate(ctx: Context) {
        val uri = Uri.parse("market://details?id=" + ctx.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            ctx.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, "Couldn't launch the market", Toast.LENGTH_LONG).show()
        }

        //AppRatingDialog.setInstalltionTime(Long.MAX_VALUE); //never show the rating dialog :)
    }

    @JvmStatic
    fun translate(ctx: Context) {
        val url = "https://crowdin.com/project/prayer-times-android"
        openUrl(ctx, url)
    }

    @JvmStatic
    fun reportBug(ctx: Context) {
        val url = "https://github.com/metinkale38/prayer-times-android/issues"
        openUrl(ctx, url)
    }

    @JvmStatic
    fun mail(ctx: Context) {
        val emailIntent =
            Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@prayerapp.dev", null))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            ctx.getString(R.string.appName) + " (com.metinkaler.prayer)"
        )
        var versionCode = "Undefined"
        var versionName = "Undefined"
        try {
            versionCode =
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode.toString() + ""
            versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName + ""
        } catch (e: PackageManager.NameNotFoundException) {
            recordException(e)
        }
        emailIntent.putExtra(
            Intent.EXTRA_TEXT,
            """
                ===Device Information===
                UUID: $UUID
                Manufacturer: ${Build.MANUFACTURER}
                Model: ${Build.MODEL}
                Android Version: ${Build.VERSION.RELEASE}
                App Version Name: $versionName
                App Version Code: $versionCode
                ======================
                

                """.trimIndent()
        )
        ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.mail)))
    }

    @JvmStatic
    fun beta(ctx: Context) {
        val url = "https://play.google.com/apps/testing/com.metinkale.prayer"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        ctx.startActivity(i)
    }
}