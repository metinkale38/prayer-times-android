package com.metinkale.prayerapp.vakit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.App
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.widgets.R

object WidgetUtils {

    fun getTheme(widgetId: Int): Theme {
        val widgets = App.get().getSharedPreferences("widgets", 0)
        val t = widgets.getInt(widgetId.toString() + "_theme", 0)
        val theme: Theme = when (t) {
            1 -> Theme.Dark
            2 -> Theme.LightTrans
            3 -> Theme.Trans
            else -> Theme.Light
        }
        return theme
    }

    fun getTimes(widgetId: Int): Times? {
        val widgets = App.get().getSharedPreferences("widgets", 0)
        val id = runCatching {
            widgets.getInt(widgetId.toString() + "", 0).takeIf { it != 0 }
        }.getOrNull()
            ?: widgets.getLong(widgetId.toString() + "", 0L).toInt()
        var t: Times? = null
        if (id != 0) {
            t = Times.getTimesById(id).current
        }
        if (t == null) widgets.edit().remove(widgetId.toString() + "").apply()
        return t
    }

    fun showNoCityWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_city_removed)
        val i = Intent(context, WidgetConfigure::class.java)
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        i.putExtra(WidgetConfigure.Companion.ONLYCITY, true)
        remoteViews.setOnClickPendingIntent(
            R.id.image,
            PendingIntent.getActivity(
                context,
                widgetId,
                i,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    fun getSize(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        aspectRatio: Float
    ): Size {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val isPort = context.resources.getBoolean(R.bool.isPort)
        val w =
            options.getInt(if (isPort) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val h =
            options.getInt(if (isPort) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        return Size(w, h, aspectRatio)
    }

    class Size(w: Int, h: Int, aspectRatio: Float) {
        val width: Int
        val height: Int

        init {
            val r = App.get().resources
            val w = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                w.toFloat(),
                r.displayMetrics
            ).toInt()
            val h = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                h.toFloat(),
                r.displayMetrics
            ).toInt()
            val w1 = (h * aspectRatio).toInt()
            val h1 = (w / aspectRatio).toInt()
            width = Math.min(w, w1)
            height = Math.min(h, h1)
        }
    }


}